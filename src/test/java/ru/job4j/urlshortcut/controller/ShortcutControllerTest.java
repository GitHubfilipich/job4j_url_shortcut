package ru.job4j.urlshortcut.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import ru.job4j.urlshortcut.service.shortcut.ShortcutService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortcutControllerTest {

    private ShortcutService shortcutService;
    private ShortcutController controller;

    @BeforeEach
    void init() {
        shortcutService = mock(ShortcutService.class);
        controller = new ShortcutController(shortcutService);
    }

    /**
     * Проверяет успешный сценарий конвертации url методом {@code convertUrl}
     */
    @Test
    void whenConvertUrlThenReturnCode() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("login");
        Map<String, String> request = Map.of("url", "http://test.ru");

        ArgumentCaptor<String> loginCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.register(loginCaptor.capture(), urlCaptor.capture())).thenReturn("code123");

        ResponseEntity<Map<String, String>> response = controller.convertUrl(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("code", "code123");
        assertThat(loginCaptor.getValue()).isEqualTo("login");
        assertThat(urlCaptor.getValue()).isEqualTo("http://test.ru");
    }

    /**
     * Проверяет успешный сценарий редиректа методом {@code redirect}
     */
    @Test
    void whenRedirectThenReturnFoundWithLocation() {
        ArgumentCaptor<String> shortcutCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.getUrlAndIncreaseCounter(shortcutCaptor.capture())).thenReturn("http://target.ru");

        ResponseEntity<Void> response = controller.redirect("abc123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("http://target.ru");
        assertThat(shortcutCaptor.getValue()).isEqualTo("abc123");
    }

    /**
     * Проверяет сценарий, когда shortcut не найден при редиректе
     */
    @Test
    void whenRedirectNotFoundThenReturnNotFound() {
        ArgumentCaptor<String> shortcutCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.getUrlAndIncreaseCounter(shortcutCaptor.capture())).thenReturn(null);

        ResponseEntity<Void> response = controller.redirect("notfound");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(shortcutCaptor.getValue()).isEqualTo("notfound");
    }

    /**
     * Проверяет успешный сценарий получения статистики методом {@code getStatistic}
     */
    @Test
    void whenGetStatisticThenReturnList() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("login");
        List<Map<String, Object>> stats = List.of(
                Map.of("url", "http://test.ru/1", "total", 10L),
                Map.of("url", "http://test.ru/2", "total", 20L)
        );
        ArgumentCaptor<String> loginCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.getStatistics(loginCaptor.capture())).thenReturn(stats);

        ResponseEntity<List<Map<String, Object>>> response = controller.getStatistic(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(stats);
        assertThat(loginCaptor.getValue()).isEqualTo("login");
    }
}