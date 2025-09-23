package ru.job4j.urlshortcut.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.urlshortcut.dto.request.LoginRequestDTO;
import ru.job4j.urlshortcut.dto.request.SignupRequestDTO;
import ru.job4j.urlshortcut.dto.response.JwtResponseDTO;
import ru.job4j.urlshortcut.dto.response.MessageResponseDTO;
import ru.job4j.urlshortcut.dto.response.RegisterDTO;
import ru.job4j.urlshortcut.jwt.JwtUtils;
import ru.job4j.urlshortcut.service.user.UserService;
import ru.job4j.urlshortcut.userdetails.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "AuthController", description = "AuthController management APIs")
@RestController
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/registration")
    public ResponseEntity<MessageResponseDTO> registerUser(@Valid @RequestBody SignupRequestDTO signUpRequest) {
        RegisterDTO registerDTO = userService.signUp(signUpRequest);
        return ResponseEntity.status(registerDTO.getStatus())
                .body(new MessageResponseDTO(registerDTO.getMessage(), registerDTO.getLogin(),
                        registerDTO.getPassword(), registerDTO.isRegistration()));
    }

    @PostMapping("/auth")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getLogin(),
                        loginRequestDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity
                .ok(new JwtResponseDTO(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getSite()));
    }
}
