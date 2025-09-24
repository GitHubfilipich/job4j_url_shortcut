package ru.job4j.urlshortcut.service.shortcut;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
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
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShortcutRepository shortcutRepository;

    @Override
    public String register(String login, String url) {
        Optional<Shortcut> shortcut = shortcutRepository.findByUrl(url);
        if (shortcut.isPresent()) {
            return shortcut.get().getShortcut();
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Site не найден"));
        String shortUrl = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Shortcut newShortcut = new Shortcut(null, url, shortUrl, user, 0L);
        shortcutRepository.save(newShortcut);
        return shortUrl;
    }

    @Override
    public String getUrlAndIncreaseCounter(String shortcut) {
        Optional<Shortcut> optionalShortcut = shortcutRepository.findByShortcut(shortcut);
        if (optionalShortcut.isEmpty()) {
            return null;
        }
        shortcutRepository.incrementCounter(optionalShortcut.get().getId());
        return optionalShortcut.get().getUrl();
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
