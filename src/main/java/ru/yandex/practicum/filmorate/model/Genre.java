package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Genre {

    private int id;
    @NotBlank
    private String name;

    public Genre() {
    }
}
