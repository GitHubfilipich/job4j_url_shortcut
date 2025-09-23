package ru.job4j.urlshortcut.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.job4j.urlshortcut.model.validation.ValidOperation;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shortcut")
@Schema(description = "Shortcut Model Information")
public class Shortcut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "ID не может быть null при обновлении",
            groups = {ValidOperation.OnUpdate.class, ValidOperation.OnDelete.class})
    @Schema(description = "Shortcut ID", example = "1")
    private Integer id;

    @NotBlank(message = "Url не может быть пустым")
    @Schema(description = "Shortcut url", example = "https://job4j.ru/exercise/214/task-view/1214")
    private String url;

    @NotBlank(message = "Shortcut не может быть пустым")
    @Schema(description = "Shortcut", example = "ZRUfdD21")
    private String shortcut;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "Пользователь shortcut не может быть null")
    @Schema(description = "Shortcut user",
            example = "{\"id\":2,\"login\":\"userLogin\",\"password\":\"pass123\",\"site\":\"example.com\"}")
    User user;

    @Schema(description = "Total", example = "99")
    Long total;
}
