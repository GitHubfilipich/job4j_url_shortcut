package ru.job4j.urlshortcut.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String login;
    private String site;

    public JwtResponseDTO(String accessToken, Long id, String login, String site) {
        this.token = accessToken;
        this.id = id;
        this.login = login;
        this.site = site;
    }
}
