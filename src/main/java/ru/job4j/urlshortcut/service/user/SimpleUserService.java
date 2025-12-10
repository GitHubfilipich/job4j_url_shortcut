package ru.job4j.urlshortcut.service.user;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.exception.LoginGenerationException;
import ru.job4j.urlshortcut.exception.SiteAlreadyRegisteredException;
import ru.job4j.urlshortcut.exception.Utils;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SimpleUserService implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    @Override
    public RegisterDTO signUp(SignupRequestDTO signUpRequest) {
        int attempts = 0;
        int maxAttempts = 5;

        while (attempts < maxAttempts) {
            attempts++;

            String login = UUID.randomUUID().toString().substring(0, 8);
            String password = createPassword();
            User user = new User(null, login, encoder.encode(password), signUpRequest.getSite());

            try {
                userRepository.save(user);
                return new RegisterDTO(HttpStatus.OK, "Site registered successfully!", login, password, true);

            } catch (DataIntegrityViolationException e) {
                if (Utils.containsConstraintName(e, "uk_user_login")) {
                    continue;
                }

                if (Utils.containsConstraintName(e, "uk_user_site")) {
                    throw new SiteAlreadyRegisteredException();
                }

                throw e;
            }
        }

        throw new LoginGenerationException();
    }

    private String createPassword() {
        String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,-.:;<=>?@";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(symbols.charAt(rnd.nextInt(symbols.length())));
        }
        return sb.toString();
    }
}
