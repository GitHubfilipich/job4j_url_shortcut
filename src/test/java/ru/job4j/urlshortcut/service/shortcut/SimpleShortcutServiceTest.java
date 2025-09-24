package ru.job4j.urlshortcut.service.shortcut;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.ShortcutRepository;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
        User user = new User(1, "login", "pass", "site.com");
        when(shortcutRepository.findByUrl("http://test.ru")).thenReturn(Optional.empty());
        when(userRepository.findByLogin("login")).thenReturn(Optional.of(user));
        when(shortcutRepository.save(any(Shortcut.class))).thenAnswer(inv -> inv.getArgument(0));

        String result = service.register("login", "http://test.ru");
        assertThat(result).isNotBlank().hasSize(8);

        ArgumentCaptor<Shortcut> captor = ArgumentCaptor.forClass(Shortcut.class);
        verify(shortcutRepository).save(captor.capture());
        Shortcut saved = captor.getValue();
        assertThat(saved.getUrl()).isEqualTo("http://test.ru");
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getShortcut()).isEqualTo(result);
        assertThat(saved.getTotal()).isEqualTo(0L);
    }

    /**
     * Проверяет сценарий, когда shortcut уже существует (findByUrl возвращает isPresent)
     */
    @Test
    void whenRegisterWithExistingShortcutThenReturnExistingShortcut() {
        Shortcut shortcut = new Shortcut(1, "http://test.ru", "abc12345", null, 0L);
        when(shortcutRepository.findByUrl("http://test.ru")).thenReturn(Optional.of(shortcut));

        String result = service.register("login", "http://test.ru");
        assertThat(result).isEqualTo("abc12345");
        verify(shortcutRepository, never()).save(any());
    }

    /**
     * Проверяет успешный сценарий получения url и инкрементации счетчика методом {@code getUrlAndIncreaseCounter}
     */
    @Test
    void whenGetUrlAndIncreaseCounterThenReturnUrl() {
        Shortcut shortcut = new Shortcut(1, "http://test.ru", "abc12345", null, 5L);
        when(shortcutRepository.findByShortcut("abc12345")).thenReturn(Optional.of(shortcut));

        String url = service.getUrlAndIncreaseCounter("abc12345");
        assertThat(url).isEqualTo("http://test.ru");
        verify(shortcutRepository).incrementCounter(1);
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
     * Проверяет сценарий, когда пользователь не найден при регистрации shortcut
     */
    @Test
    void whenRegisterWithUnknownLoginThenThrowException() {
        when(shortcutRepository.findByUrl("http://test.ru")).thenReturn(Optional.empty());
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register("unknown", "http://test.ru"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Site не найден");
    }
}