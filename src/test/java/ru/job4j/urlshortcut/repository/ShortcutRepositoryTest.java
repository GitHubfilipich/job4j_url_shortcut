package ru.job4j.urlshortcut.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.job4j.urlshortcut.model.Shortcut;
import ru.job4j.urlshortcut.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class ShortcutRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private ShortcutRepository shortcutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Проверяет успешный сценарий поиска по пользователю методом {@code findByUser}
     */
    @Test
    void whenFindByUserThenReturnShortcuts() {
        User user = userRepository.save(new User(null, "login4", "pass4", "site4.com"));
        Shortcut shortcut1 = shortcutRepository.save(new Shortcut(null, "http://user.ru/1", "u1", user, 0L));
        Shortcut shortcut2 = shortcutRepository.save(new Shortcut(null, "http://user.ru/2", "u2", user, 0L));

        User user1 = userRepository.save(new User(null, "login5", "pass5", "site5.com"));
        Shortcut shortcut3 = shortcutRepository.save(new Shortcut(null, "http://user.ru/3", "u3", user1, 0L));

        List<Shortcut> shortcuts = shortcutRepository.findByUser(user);

        assertThat(shortcuts).hasSize(2);
        assertThat(shortcuts).extracting(Shortcut::getShortcut).containsExactlyInAnyOrder("u1", "u2");
    }

    /**
     * Проверяет успешный сценарий получения shortcut и инкремента счётчика методом
     * {@code findByShortcutAndIncrementCounter}
     */
    @Test
    void whenFindByShortcutAndIncrementCounterThenReturnShortcut() {
        User user = userRepository.save(new User(null, "login6", "pass6", "site6.com"));
        Shortcut saved = shortcutRepository.save(new Shortcut(null, "http://test.ru/incr", "sc6", user, 0L));
        entityManager.flush();
        entityManager.clear();

        Optional<Shortcut> opt = shortcutRepository.findByShortcutAndIncrementCounter("sc6");

        assertThat(opt).isPresent();
        Shortcut fetched = opt.get();
        assertThat(fetched.getShortcut()).isEqualTo("sc6");
        assertThat(fetched.getUrl()).isEqualTo("http://test.ru/incr");
        assertThat(fetched.getUser()).usingRecursiveComparison().isEqualTo(user);
        assertThat(fetched.getTotal()).isEqualTo(1L);
    }
}