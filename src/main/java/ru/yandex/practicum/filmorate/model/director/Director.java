package ru.yandex.practicum.filmorate.model.director;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.*;

/**
 * Класс описывает модель режиссера.
 */
@Data
@AllArgsConstructor
public class Director {
    /**
     * Целочисленный идентификатор режиссера.
     */
    @Digits(integer = Integer.MAX_VALUE, fraction = 0)
    private int id;
    /**
     * Имя режиссера.
     */
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s]+$") // только латинские буквы
    private String name;
}
