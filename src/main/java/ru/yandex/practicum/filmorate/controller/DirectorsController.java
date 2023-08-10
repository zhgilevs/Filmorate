package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

/**
 * Класс-контролер, обслуживающий режисеров.
 */
@RestController
@RequestMapping("/directors")
@Validated
@RequiredArgsConstructor
@Slf4j
public class DirectorsController {
    /**
     * Сервис директоров.
     */
    private final DirectorService directorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Director create(@RequestBody @Valid Director director) {
        log.info("POST /directors: " + director);
        return directorService.create(director);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    Director update(@RequestBody @Valid Director director) {
        log.info("PUT /directors: " + director);
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    Director delete(@PathVariable int id) {
        log.info("DELETE /directors/" + id);
        return directorService.delete(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<Director> getAll() {
        log.info("GET /directors");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    Director getDirectorById(@PathVariable int id) {
        log.info("GET /directors/" + id);
        return directorService.getById(id);
    }
}
