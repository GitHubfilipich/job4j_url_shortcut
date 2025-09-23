package ru.job4j.urlshortcut.service.shortcut;

import java.util.List;
import java.util.Map;

public interface ShortcutService {
    String register(String login, String url);

    String getUrlAndIncreaseCounter(String shortcut);

    List<Map<String, Object>> getStatistics(String siteLogin);
}
