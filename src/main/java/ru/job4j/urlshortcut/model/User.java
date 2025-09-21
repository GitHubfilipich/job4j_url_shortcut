package ru.job4j.urlshortcut.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.job4j.urlshortcut.model.validation.ValidOperation;

import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shortcut_user")
@Schema(description = "User Model Information")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "ID не может быть null при обновлении",
            groups = {ValidOperation.OnUpdate.class, ValidOperation.OnDelete.class})
    @Schema(description = "User ID", example = "1")
    private Integer id;

    @NotBlank(message = "Login не может быть пустым")
    @Schema(description = "User login", example = "userLogin")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "User password", example = "qwerty123")
    private String password;

    @NotBlank(message = "Site не может быть пустым")
    @Schema(description = "User site", example = "example.com")
    private String site;
}

