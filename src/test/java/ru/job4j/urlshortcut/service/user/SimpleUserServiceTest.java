package ru.job4j.urlshortcut.service.user;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleUserServiceTest {
    /**
     * Проверяет успешный сценарий регистрации нового сайта методом {@code signUp}
     */
    @Test
    void whenSignUpThenRegisterNewSite() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findBySite("newsite.com")).thenReturn(Optional.empty());
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
     * Проверяет неуспешный сценарий регистрации нового сайта методом {@code signUp}
     */
    @Test
    void whenSignUpWithExistingSiteThenError() {
        SignupRequestDTO request = new SignupRequestDTO("exist.com");
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findBySite(request.getSite())).thenReturn(
                Optional.of(new User(1, "login", "pass", request.getSite())));

        SimpleUserService service = new SimpleUserService(userRepository, new BCryptPasswordEncoder());

        RegisterDTO response = service.signUp(request);

        assertThat(response.isRegistration()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error: site is already registered!");
        verify(userRepository, never()).save(any());
    }
}