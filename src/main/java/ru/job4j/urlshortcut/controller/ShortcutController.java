package ru.job4j.urlshortcut.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.job4j.urlshortcut.exception.ShortcutGenerationException;
import ru.job4j.urlshortcut.exception.UrlAlreadyRegisteredException;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.service.shortcut.ShortcutService;
import ru.job4j.urlshortcut.userdetails.UserDetailsImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "ShortcutController", description = "ShortcutController management APIs")
@RestController
@AllArgsConstructor
public class ShortcutController {
    private final ShortcutService shortcutService;

    @PostMapping("/convert")
    public ResponseEntity<Map<String, String>> convertUrl(@RequestBody Map<String, String> request,
                                                          @AuthenticationPrincipal UserDetailsImpl principal) {
        User user = principal.getUser();
        String url = request.get("url");
        String code = shortcutService.register(user, url);
        return ResponseEntity.ok(Map.of("code", code));
    }

    @GetMapping("/redirect/{shortcut}")
    public ResponseEntity<Void> redirect(@PathVariable String shortcut) {
        Optional<String> targetUrl = shortcutService.getUrlAndIncreaseCounter(shortcut);
        if (targetUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, targetUrl.get())
                .build();
    }

    @GetMapping("/statistic")
    public ResponseEntity<List<Map<String, Object>>> getStatistic(Authentication authentication) {
        String siteLogin = authentication.getName();
        List<Map<String, Object>> stats = shortcutService.getStatistics(siteLogin);
        return ResponseEntity.ok(stats);
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class, ShortcutGenerationException.class,
            UrlAlreadyRegisteredException.class})
    public void catchDataIntegrityViolationException(Exception e, HttpServletRequest request,
                                                     HttpServletResponse response) throws IOException {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (e instanceof ShortcutGenerationException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, String> details = new HashMap<>();
        details.put("message", e.getMessage());
        details.put("type", String.valueOf(e.getClass()));
        details.put("timestamp", String.valueOf(LocalDateTime.now()));
        details.put("path", request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(details));
    }
}
