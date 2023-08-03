package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validateUser(review.getUserId());
        validateFilm(review.getFilmId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        validateReview(review.getReviewId());
        return reviewStorage.update(review);
    }

    public boolean remove(int id) {
        validateReview(id);
        return reviewStorage.remove(id);
    }

    public Review getById(int id) {
        validateReview(id);
        return reviewStorage.getById(id);
    }

    public List<Review> getAllOrByFilmId(Optional<Integer> oFilmId, Optional<Integer> oCount) {
        int filmId = oFilmId.orElse(0);
        int count = oCount.orElse(10);
        if (filmId == 0) {
            return reviewStorage.get(count);
        } else {
            validateFilm(filmId);
            return reviewStorage.getByFilmId(filmId, count);
        }
    }

    public Review setLike(int id, int userId) {
        validate(id, userId);
        return reviewStorage.setLike(id, userId);
    }

    public Review setDislike(int id, int userId) {
        validate(id, userId);
        return reviewStorage.setDislike(id, userId);
    }

    public boolean removeLike(int id, int userId) {
        validate(id, userId);
        return reviewStorage.removeLike(id, userId);
    }

    public boolean removeDislike(int id, int userId) {
        validate(id, userId);
        return reviewStorage.removeDislike(id, userId);
    }

    private void validateReview(int id) {
        if (!reviewStorage.isExists(id)) {
            throw new NotFoundException(String.format("Отзыв с ID: '%s' не найден", id));
        }
    }

    private void validateUser(int userId) {
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID: '%s' не найден", userId));
        }
    }

    private void validateFilm(int filmId) {
        if (!filmStorage.isExists(filmId)) {
            throw new NotFoundException(String.format("Фильм с ID: '%s' не найден", filmId));
        }
    }

    private void validate(int id, int userId) {
        validateUser(userId);
        validateReview(id);
    }
}
