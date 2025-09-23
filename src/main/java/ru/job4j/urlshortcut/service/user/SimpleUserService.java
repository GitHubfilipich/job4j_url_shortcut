package ru.job4j.urlshortcut.service.user;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.model.User;
import ru.job4j.urlshortcut.repository.UserRepository;

import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
@Service
public class SimpleUserService implements UserService {
    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder encoder;

    @Override
    public RegisterDTO signUp(SignupRequestDTO signUpRequest) {
        if (userRepository.findBySite(signUpRequest.getSite()).isPresent()) {
            return new RegisterDTO(HttpStatus.BAD_REQUEST, "Error: site is already registered!", null, null, false);
        }
        String login = UUID.randomUUID().toString().substring(0, 8);
        String password = createPassword();
        User user = new User(null, login, encoder.encode(password), signUpRequest.getSite());
        userRepository.save(user);
        return new RegisterDTO(HttpStatus.OK, "Site registered successfully!", login, password, true);
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
