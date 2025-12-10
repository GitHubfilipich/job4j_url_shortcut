package ru.job4j.urlshortcut.service.shortcut;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.job4j.urlshortcut.exception.ShortcutGenerationException;
import ru.job4j.urlshortcut.exception.UrlAlreadyRegisteredException;
import ru.job4j.urlshortcut.exception.Utils;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.ShortcutRepository;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SimpleShortcutService implements ShortcutService {
    private final UserRepository userRepository;

    private final ShortcutRepository shortcutRepository;

    @Override
    public String register(User user, String url) {
        int attempts = 0;
        int maxAttempts = 5;

        while (attempts < maxAttempts) {
            attempts++;

            String shortUrl = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            Shortcut newShortcut = new Shortcut(null, url, shortUrl, user, 0L);

            try {
                shortcutRepository.save(newShortcut);
                return shortUrl;
            } catch (DataIntegrityViolationException e) {
                if (Utils.containsConstraintName(e, "uk_shortcut_shortcut")) {
                    continue;
                }

                if (Utils.containsConstraintName(e, "uk_shortcut_url")) {
                    throw new UrlAlreadyRegisteredException();
                }

                throw e;
            }
        }

        throw new ShortcutGenerationException();
    }

    @Override
    public Optional<String> getUrlAndIncreaseCounter(String shortcut) {
        return shortcutRepository.findByShortcutAndIncrementCounter(shortcut)
                .map(Shortcut::getUrl);
    }

    @Override
    public List<Map<String, Object>> getStatistics(String siteLogin) {
        User user = userRepository.findByLogin(siteLogin)
                .orElseThrow(() -> new UsernameNotFoundException("Site не найден"));

        return shortcutRepository.findByUser(user).stream()
                .map(e -> Map.<String, Object>of("url", e.getUrl(), "total", e.getTotal()))
                .toList();
    }
}
