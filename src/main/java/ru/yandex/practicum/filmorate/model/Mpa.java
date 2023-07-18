package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class Mpa {
    private final int id;
    @NotBlank
    private final String name;
    @NotBlank
    private final String description;
}
