package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    Review create(@RequestBody @Valid Review review) {
        return service.create(review);
    }

    @PutMapping
    Review update(@RequestBody @Valid Review review) {
        return service.update(review);
    }

    @DeleteMapping("/{id}")
    boolean remove(@PathVariable int id) {
        return service.remove(id);
    }

    @GetMapping("/{id}")
    Review getById(@PathVariable(value = "id") int id) {
        return service.getById(id);
    }

    @GetMapping
    List<Review> getAllOrByFilmId(@RequestParam Optional<Integer> filmId,
                                  @RequestParam Optional<Integer> count) {
        return service.getAllOrByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    Review setLike(@PathVariable int id,
                   @PathVariable int userId) {
        return service.setLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    Review setDislike(@PathVariable int id,
                      @PathVariable int userId) {
        return service.setDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    boolean removeLike(@PathVariable int id,
                       @PathVariable int userId) {
        return service.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    boolean removeDislike(@PathVariable int id,
                          @PathVariable int userId) {
        return service.removeDislike(id, userId);
    }
}