package ru.job4j.urlshortcut.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.job4j.urlshortcut.service.shortcut.ShortcutService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "ShortcutController", description = "ShortcutController management APIs")
@RestController
@AllArgsConstructor
public class ShortcutController {
    @Autowired
    private ShortcutService shortcutService;

    @PostMapping("/convert")
    public ResponseEntity<Map<String, String>> convertUrl(@RequestBody Map<String, String> request,
                                                          Authentication authentication) {
        String login = authentication.getName();
        String url = request.get("url");
        String code = shortcutService.register(login, url);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @GetMapping("/redirect/{shortcut}")
    public ResponseEntity<Void> redirect(@PathVariable String shortcut) {
        String targetUrl = shortcutService.getUrlAndIncreaseCounter(shortcut);
        if (targetUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, targetUrl)
                .build();
    }

    @GetMapping("/statistic")
    public ResponseEntity<List<Map<String, Object>>> getStatistic(Authentication authentication) {
        String siteLogin = authentication.getName();
        List<Map<String, Object>> stats = shortcutService.getStatistics(siteLogin);
        return ResponseEntity.ok(stats);
    }
}
