package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
public class Film {

    private int id;
    private int rate;
    @NotBlank
    @Size(min = 1, max = 200, message = "Описание должно быть не более 200 символов и не менее 1 символа")
    private String description;
    @NotNull
    @Positive
    private int duration;
    @NotBlank
    private String name;
    private LocalDate releaseDate;

}