package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.model.director.Marker;
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
    @Validated({Marker.OnCreate.class})
    @ResponseStatus(HttpStatus.CREATED)
    Director create(@RequestBody @Valid Director director) {
        log.info("POST /directors: " + director);
        return directorService.create(director);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    @ResponseStatus(HttpStatus.OK)
    Director update(@RequestBody @Valid Director director) {
        log.info("PUT /directors: " + director);
        return directorService.update(director);
    }

    @DeleteMapping
    @Validated({Marker.OnUpdate.class})
    @ResponseStatus(HttpStatus.OK)
    Director delete(@RequestBody @Valid Director director) {
        log.info("DELETE /directors: " + director);
        return directorService.delete(director);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<Director> getAll() {
        log.info("GET /directors");
        return directorService.getAll();
    }

    @GetMapping("/directors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Director getDirectorById(@PathVariable int id) {
        log.info("GET /directors/" + id);
        return directorService.getById(id);
    }
}
