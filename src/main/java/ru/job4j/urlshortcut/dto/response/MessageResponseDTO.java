package ru.job4j.urlshortcut.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO {
    private String message;
    private String login;
    private String password;
    boolean registration;
}
