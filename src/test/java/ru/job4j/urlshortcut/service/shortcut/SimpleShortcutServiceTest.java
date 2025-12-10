package ru.job4j.urlshortcut.service.shortcut;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import ru.job4j.urlshortcut.exception.ShortcutGenerationException;
import ru.job4j.urlshortcut.exception.UrlAlreadyRegisteredException;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.ShortcutRepository;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SimpleShortcutServiceTest {

    private UserRepository userRepository;
    private ShortcutRepository shortcutRepository;
    private SimpleShortcutService service;

    @BeforeEach
    void init() {
        userRepository = mock(UserRepository.class);
        shortcutRepository = mock(ShortcutRepository.class);
        service = new SimpleShortcutService(userRepository, shortcutRepository);
    }

    /**
     * Проверяет успешный сценарий регистрации shortcut методом {@code register}
     */
    @Test
    void whenRegisterThenReturnShortcut() {
        String url = "http://test.ru";
        User user = new User(1, "login", "pass", "site.com");
        ArgumentCaptor<Shortcut> captor = ArgumentCaptor.forClass(Shortcut.class);
        when(shortcutRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        String result = service.register(user, url);
        Shortcut saved = captor.getValue();

        assertThat(result).isNotBlank().hasSize(8);
        assertThat(saved.getUrl()).isEqualTo(url);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getShortcut()).isEqualTo(result);
        assertThat(saved.getTotal()).isEqualTo(0L);
    }

    /**
     * Проверяет успешный сценарий получения статистики методом {@code getStatistics}
     */
    @Test
    void whenGetStatisticsThenReturnList() {
        User user = new User(1, "login", "pass", "site.com");
        Shortcut shortcut1 = new Shortcut(1, "http://test.ru/1", "s1", user, 10L);
        Shortcut shortcut2 = new Shortcut(2, "http://test.ru/2", "s2", user, 20L);

        when(userRepository.findByLogin("login")).thenReturn(Optional.of(user));
        when(shortcutRepository.findByUser(user)).thenReturn(List.of(shortcut1, shortcut2));

        List<Map<String, Object>> stats = service.getStatistics("login");
        assertThat(stats).hasSize(2);
        assertThat(stats.get(0)).containsEntry("url", "http://test.ru/1").containsEntry("total", 10L);
        assertThat(stats.get(1)).containsEntry("url", "http://test.ru/2").containsEntry("total", 20L);
    }

    /**
     * Проверяет неуспешный сценарий регистрации shortcut методом {@code register} из-за повторной регистрации URL
     */
    @Test
    void whenRegisterWithExistingUrlThenError() {
        String url = "http://exists.ru";
        User user = new User(1, "login", "pass", "site.com");

        String message = "uk_shortcut_url";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message,
                new ConstraintViolationException(message, new SQLException(message), message));
        when(shortcutRepository.save(any(Shortcut.class))).thenThrow(exception);

        assertThrows(UrlAlreadyRegisteredException.class, () -> service.register(user, url));
    }

    /**
     * Проверяет неуспешный сценарий регистрации shortcut методом {@code register} из-за невозможности
     * сгенерировать уникальный shortcut после нескольких попыток
     */
    @Test
    void whenRegisterWithFailedToGenerateUniqueShortcutThenError() {
        String url = "http://new.ru";
        User user = new User(1, "login", "pass", "site.com");

        String message = "uk_shortcut_shortcut";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message,
                new ConstraintViolationException(message, new SQLException(message), message));
        when(shortcutRepository.save(any(Shortcut.class))).thenThrow(exception);

        assertThrows(ShortcutGenerationException.class, () -> service.register(user, url));
        verify(shortcutRepository, times(5)).save(any());
    }

    /**
     * Проверяет успешный сценарий получения URL и инкремента счётчика методом {@code getUrlAndIncreaseCounter}
     */
    @Test
    void whenGetUrlAndIncreaseCounterThenReturnUrl() {
        String sc = "abcd1234";
        User user = new User(1, "login", "pass", "site.com");
        String url = "http://test.ru";
        Shortcut shortcut = new Shortcut(1, url, sc, user, 5L);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(shortcutRepository.findByShortcutAndIncrementCounter(captor.capture()))
                .thenReturn(Optional.of(shortcut));

        Optional<String> result = service.getUrlAndIncreaseCounter(sc);

        assertThat(result).isPresent().contains(url);
        assertThat(captor.getValue()).isEqualTo(sc);
    }

    /**
     * Проверяет сценарий регистрации shortcut методом {@code register}, когда возникает
     * DataIntegrityViolationException не связанная с uk_shortcut_shortcut или uk_shortcut_url — исключение должно быть
     * проброшено
     */
    @Test
    void whenRegisterWithUnknownDbErrorThenThrow() {
        String url = "http://unknown.ru";
        User user = new User(1, "login", "pass", "site.com");

        DataIntegrityViolationException unknownEx = new DataIntegrityViolationException("unknown db error");
        when(shortcutRepository.save(any(Shortcut.class))).thenThrow(unknownEx);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                () -> service.register(user, url));
        assertThat(thrown).isSameAs(unknownEx);
    }
}