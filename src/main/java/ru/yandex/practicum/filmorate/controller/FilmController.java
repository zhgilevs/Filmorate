package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int globalId = 0;
    private static final LocalDate CINEMA_STARTING_POINT = LocalDate.of(1895, 12, 28);

    @PostMapping
    Film create(@RequestBody @Valid Film film) {
        validateReleaseDate(film);
        int id = ++globalId;
        film.setId(id);
        films.put(id, film);
        log.info("Фильм: '{}' создан", film);
        return film;
    }

    @PutMapping
    Film update(@RequestBody @Valid Film updatedFilm) {
        validateReleaseDate(updatedFilm);
        int id = updatedFilm.getId();
        if (films.containsKey(id)) {
            films.put(id, updatedFilm);
            log.info("Фильм с ID: '{}' обновлен", id);
            return updatedFilm;
        } else {
            log.warn("Фильма нет в базе");
            throw new ValidationException("Фильм с ID: '" + id + "' отсутствует в базе");
        }
    }

    @GetMapping
    List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза не передана в запросе");
            throw new ValidationException("Дата релиза должна быть передана в запросе");
        }
        if (film.getReleaseDate().isBefore(CINEMA_STARTING_POINT)) {
            log.warn("Ошибка в дате релиза");
            throw new ValidationException("Дата релиза должна быть позже 1895-12-28");
        }
    }
}
