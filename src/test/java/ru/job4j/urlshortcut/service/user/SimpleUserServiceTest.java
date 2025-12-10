package ru.job4j.urlshortcut.service.user;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.exception.LoginGenerationException;
import ru.job4j.urlshortcut.exception.SiteAlreadyRegisteredException;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SimpleUserServiceTest {
    /**
     * Проверяет успешный сценарий регистрации нового сайта методом {@code signUp}
     */
    @Test
    void whenSignUpThenRegisterNewSite() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SimpleUserService service = new SimpleUserService(userRepository, new BCryptPasswordEncoder());
        SignupRequestDTO request = new SignupRequestDTO("newsite.com");
        RegisterDTO response = service.signUp(request);

        assertThat(response.isRegistration()).isTrue();
        assertThat(response.getLogin()).isNotBlank();
        assertThat(response.getPassword()).isNotBlank();
        assertThat(response.getMessage()).isEqualTo("Site registered successfully!");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getSite()).isEqualTo("newsite.com");
        assertThat(savedUser.getLogin()).isEqualTo(response.getLogin());
        assertThat(savedUser.getPassword()).isNotBlank();
    }

    /**
     * Проверяет неуспешный сценарий регистрации нового сайта методом {@code signUp} из-за повторной регистрации сайта
     */
    @Test
    void whenSignUpWithExistingSiteThenError() {
        UserRepository userRepository = mock(UserRepository.class);
        String message = "uk_user_site";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message,
                new ConstraintViolationException(message, new SQLException(message), message));
        when(userRepository.save(any(User.class))).thenThrow(exception);

        SimpleUserService service = new SimpleUserService(userRepository, new BCryptPasswordEncoder());
        SignupRequestDTO request = new SignupRequestDTO("newsite.com");

        assertThrows(SiteAlreadyRegisteredException.class, () -> service.signUp(request));
    }

    /**
     * Проверяет неуспешный сценарий регистрации нового сайта методом {@code signUp} из-за невозможности
     * сгенерировать уникальный логин после нескольких попыток
     */
    @Test
    void whenSignUpWithFailedToGenerateUniqueLoginThenError() {
        UserRepository userRepository = mock(UserRepository.class);
        String message = "uk_user_login";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message,
                new ConstraintViolationException(message, new SQLException(message), message));
        when(userRepository.save(any(User.class))).thenThrow(exception);

        SimpleUserService service = new SimpleUserService(userRepository, new BCryptPasswordEncoder());
        SignupRequestDTO request = new SignupRequestDTO("newsite.com");

        assertThrows(LoginGenerationException.class, () -> service.signUp(request));
        verify(userRepository, times(5)).save(any());
    }

    /**
     * Проверяет сценарий регистрации нового сайта методом {@code signUp}, когда возникает
     * DataIntegrityViolationException не связанная с uk_user_login или uk_user_site — исключение должно быть проброшено
     */
    @Test
    void whenSignUpWithUnknownDbErrorThenThrow() {
        UserRepository userRepository = mock(UserRepository.class);
        DataIntegrityViolationException unknownEx = new DataIntegrityViolationException("unknown db error");
        when(userRepository.save(any(User.class))).thenThrow(unknownEx);

        SimpleUserService service = new SimpleUserService(userRepository, new BCryptPasswordEncoder());
        SignupRequestDTO request = new SignupRequestDTO("newsite.com");

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                () -> service.signUp(request));
        assertThat(thrown).isSameAs(unknownEx);
    }
}