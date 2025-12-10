package ru.job4j.urlshortcut.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.job4j.urlshortcut.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * Проверяет успешный сценарий поиска пользователя по логину методом {@code findByLogin}
     */
    @Test
    void whenFindByLoginThenReturnUser() {
        User user1 = userRepository.save(new User(null, "login1", "pass1", "site1.com"));
        User user2 = userRepository.save(new User(null, "login2", "pass2", "site2.com"));
        User user3 = userRepository.save(new User(null, "login3", "pass3", "site3.com"));
        Optional<User> found = userRepository.findByLogin("login2");
        assertThat(found).isPresent();
        assertThat(found.get()).usingRecursiveComparison().isEqualTo(user2);
    }
}