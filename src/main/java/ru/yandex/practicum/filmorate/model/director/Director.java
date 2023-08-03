package ru.yandex.practicum.filmorate.model.director;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Класс описывает модель режиссера.
 */
@Data
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
