package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@RequiredArgsConstructor
public class FilmController {

    private final FilmService service;

    @PostMapping
    Film create(@RequestBody @Valid Film film) {
        return service.create(film);
    }

    @PutMapping
    Film update(@RequestBody @Valid Film updatedFilm) {
        return service.update(updatedFilm);
    }

    @GetMapping
    List<Film> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    Film getById(@PathVariable(value = "id") int id) {
        return service.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    Film setLike(@PathVariable int id,
                 @PathVariable int userId) {
        return service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    Film removeLike(@PathVariable int id,
                    @PathVariable int userId) {
        return service.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", value = "count") int count) {
        return service.getPopular(count);
    }
}