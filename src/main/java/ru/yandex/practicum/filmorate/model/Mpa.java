package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Mpa {
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;

    public Mpa() {
    }
}
