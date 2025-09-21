package ru.job4j.urlshortcut.service.user;

import jakarta.validation.Valid;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;

public interface UserService {
    RegisterDTO signUp(SignupRequestDTO signUpRequest);
}
