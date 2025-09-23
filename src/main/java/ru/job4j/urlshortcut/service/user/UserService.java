package ru.job4j.urlshortcut.service.user;

import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;

public interface UserService {
    RegisterDTO signUp(SignupRequestDTO signUpRequest);
}
