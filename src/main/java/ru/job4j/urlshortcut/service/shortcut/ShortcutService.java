package ru.job4j.urlshortcut.service.shortcut;

import ru.job4j.urlshortcut.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ShortcutService {
    String register(User user, String url);

    Optional<String> getUrlAndIncreaseCounter(String shortcut);

    List<Map<String, Object>> getStatistics(String siteLogin);
}
