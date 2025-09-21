package ru.job4j.urlshortcut.service.user;

import org.springframework.stereotype.Service;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;

@Service
public class SimpleUserService implements UserService {
    @Override
    public RegisterDTO signUp(SignupRequestDTO signUpRequest) {
        return null;
    }
}
