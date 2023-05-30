package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
public class Film {

    private int id;
    private int rate;
    @Size(max = 200, message = "Описание должно быть не более 200 символов")
    private String description;
    @Positive
    private int duration;
    @NotBlank
    @NonNull
    private String name;
    private LocalDate releaseDate;
}