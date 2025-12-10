package ru.job4j.urlshortcut.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import ru.job4j.urlshortcut.dto.request.LoginRequestDTO;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.JwtResponseDTO;
import ru.job4j.urlshortcut.dto.response.MessageResponseDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.exception.LoginGenerationException;
import ru.job4j.urlshortcut.jwt.JwtUtils;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.service.user.UserService;
import ru.job4j.urlshortcut.userdetails.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;
    private AuthController controller;

    @BeforeEach
    void init() {
        userService = mock(UserService.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtUtils = mock(JwtUtils.class);
        controller = new AuthController(userService, authenticationManager, jwtUtils);
    }

    /**
     * Проверяет успешный сценарий регистрации пользователя методом {@code registerUser}
     */
    @Test
    void whenRegisterUserThenReturnMessageResponse() {
        SignupRequestDTO request = new SignupRequestDTO("site.com");
        RegisterDTO registerDTO = new RegisterDTO(HttpStatus.OK, "Site registered successfully!",
                "login", "password", true);
        when(userService.signUp(request)).thenReturn(registerDTO);

        ResponseEntity<MessageResponseDTO> response = controller.registerUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        MessageResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Site registered successfully!");
        assertThat(body.getLogin()).isEqualTo("login");
        assertThat(body.getPassword()).isEqualTo("password");
        assertThat(body.isRegistration()).isTrue();
    }

    /**
     * Проверяет успешный сценарий аутентификации пользователя методом {@code authenticateUser}
     */
    @Test
    void whenAuthenticateUserThenReturnJwtResponse() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("login", "password");
        Authentication authentication = mock(Authentication.class);
        User user = new User(1, "login", "pass", "site.com");
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "login", "password", "site.com", user);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

        ResponseEntity<JwtResponseDTO> response = controller.authenticateUser(loginRequestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JwtResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getId()).isEqualTo(1);
        assertThat(body.getLogin()).isEqualTo("login");
        assertThat(body.getSite()).isEqualTo("site.com");
    }

    /**
     * Проверяет обработчик исключений {@code catchDataIntegrityViolationException} в AuthController
     */
    @Test
    void whenCatchDataIntegrityViolationExceptionThenWriteDetails() throws Exception {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("db violation");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/registration");

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
        assertThat(body).contains("\"path\":\"/registration\"");
    }

    /**
     * Проверяет обработчик исключений {@code catchDataIntegrityViolationException} в AuthController
     */
    @Test
    void whenCatchLoginGenerationExceptionThenWriteDetails() throws Exception {
        LoginGenerationException ex = new LoginGenerationException();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/registration");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        controller.catchDataIntegrityViolationException(ex, request, response);

        verify(response).setStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(response).setContentType("application/json; charset=utf-8");
        pw.flush();
        String body = sw.toString();
        assertThat(body).contains("\"message\":\"Failed to generate unique login\"");
        assertThat(body).contains("\"type\"");
        assertThat(body).contains("\"path\":\"/registration\"");
    }
}