package ru.job4j.urlshortcut.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ShortcutRepositoryTest {

    @Autowired
    private ShortcutRepository shortcutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Проверяет успешный сценарий поиска по url методом {@code findByUrl}
     */
    @Test
    void whenFindByUrlThenReturnShortcut() {
        User user = userRepository.save(new User(null, "login", "pass", "site.com"));
        Shortcut shortcut1 = shortcutRepository.save(new Shortcut(null, "http://test.ru/1", "abc111", user, 0L));
        Shortcut shortcut2 = shortcutRepository.save(new Shortcut(null, "http://test.ru/2", "abc222", user, 0L));
        Shortcut shortcut3 = shortcutRepository.save(new Shortcut(null, "http://test.ru/3", "abc333", user, 0L));
        Optional<Shortcut> found = shortcutRepository.findByUrl("http://test.ru/2");
        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison().isEqualTo(shortcut2);
    }

    /**
     * Проверяет успешный сценарий поиска по shortcut методом {@code findByShortcut}
     */
    @Test
    void whenFindByShortcutThenReturnShortcut() {
        User user = userRepository.save(new User(null, "login2", "pass2", "site2.com"));
        Shortcut shortcut1 = shortcutRepository.save(new Shortcut(null, "http://test2.ru/1", "xyz111", user, 0L));
        Shortcut shortcut2 = shortcutRepository.save(new Shortcut(null, "http://test2.ru/2", "xyz222", user, 0L));
        Shortcut shortcut3 = shortcutRepository.save(new Shortcut(null, "http://test2.ru/3", "xyz333", user, 0L));
        Optional<Shortcut> found = shortcutRepository.findByShortcut("xyz333");
        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison().isEqualTo(shortcut3);
    }

    /**
     * Проверяет успешный сценарий инкрементации счетчика методом {@code incrementCounter}
     */
    @Test
    void whenIncrementCounterThenTotalIncreased() {
        User user = userRepository.save(new User(null, "login3", "pass3", "site3.com"));
        Shortcut shortcut = shortcutRepository.save(new Shortcut(null, "http://test3.ru", "inc123", user, 5L));
        shortcutRepository.incrementCounter(shortcut.getId());
        entityManager.flush();
        entityManager.clear();
        Shortcut updated = shortcutRepository.findById(shortcut.getId()).get();
        assertThat(updated.getTotal()).isEqualTo(6L);
    }

    /**
     * Проверяет успешный сценарий поиска по пользователю методом {@code findByUser}
     */
    @Test
    void whenFindByUserThenReturnShortcuts() {
        User user = userRepository.save(new User(null, "login4", "pass4", "site4.com"));
        Shortcut shortcut1 = shortcutRepository.save(new Shortcut(null, "http://user.ru/1", "u1", user, 0L));
        Shortcut shortcut2 = shortcutRepository.save(new Shortcut(null, "http://user.ru/2", "u2", user, 0L));
        List<Shortcut> shortcuts = shortcutRepository.findByUser(user);
        assertThat(shortcuts).hasSize(2);
        assertThat(shortcuts).extracting(Shortcut::getShortcut).containsExactlyInAnyOrder("u1", "u2");
    }
}