package ru.job4j.urlshortcut.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import ru.job4j.urlshortcut.exception.ShortcutGenerationException;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.service.shortcut.ShortcutService;
import ru.job4j.urlshortcut.userdetails.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        User user = new User(1, "login", "pass", "site.com");
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUser()).thenReturn(user);
        Map<String, String> request = Map.of("url", "http://test.ru");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.register(userCaptor.capture(), urlCaptor.capture())).thenReturn("code123");

        ResponseEntity<Map<String, String>> response = controller.convertUrl(request, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("code", "code123");
        assertThat(userCaptor.getValue()).isEqualTo(user);
        assertThat(urlCaptor.getValue()).isEqualTo("http://test.ru");
    }

    /**
     * Проверяет успешный сценарий редиректа методом {@code redirect}
     */
    @Test
    void whenRedirectThenReturnFoundWithLocation() {
        String url = "http://target.ru";
        String shortcut = "shortcut";
        ArgumentCaptor<String> shortcutCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.getUrlAndIncreaseCounter(shortcutCaptor.capture()))
                .thenReturn(Optional.of(url));

        ResponseEntity<Void> response = controller.redirect(shortcut);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo(url);
        assertThat(shortcutCaptor.getValue()).isEqualTo(shortcut);
    }

    /**
     * Проверяет сценарий, когда shortcut не найден при редиректе
     */
    @Test
    void whenRedirectNotFoundThenReturnNotFound() {
        ArgumentCaptor<String> shortcutCaptor = ArgumentCaptor.forClass(String.class);
        when(shortcutService.getUrlAndIncreaseCounter(shortcutCaptor.capture())).thenReturn(Optional.empty());

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

    /**
     * Проверяет обработчик исключений {@code catchDataIntegrityViolationException} в ShortcutController
     */
    @Test
    void whenCatchDataIntegrityViolationExceptionThenWriteDetails() throws Exception {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("db violation");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/convert");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        controller.catchDataIntegrityViolationException(ex, request, response);

        verify(response).setStatus(org.springframework.http.HttpStatus.BAD_REQUEST.value());
        verify(response).setContentType("application/json; charset=utf-8");
        pw.flush();
        String body = sw.toString();
        assertThat(body).contains("\"message\":\"db violation\"");
        assertThat(body).contains("\"type\"");
        assertThat(body).contains("\"path\":\"/convert\"");
    }

    /**
     * Проверяет обработчик исключений {@code catchDataIntegrityViolationException} в ShortcutController
     */
    @Test
    void whenCatchShortcutGenerationExceptionThenWriteDetails() throws Exception {
        ShortcutGenerationException ex = new ShortcutGenerationException();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/convert");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        controller.catchDataIntegrityViolationException(ex, request, response);

        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("application/json; charset=utf-8");
        pw.flush();
        String body = sw.toString();
        assertThat(body).contains("\"message\":\"Failed to generate unique shortcut\"");
        assertThat(body).contains("\"type\"");
        assertThat(body).contains("\"path\":\"/convert\"");
    }
}