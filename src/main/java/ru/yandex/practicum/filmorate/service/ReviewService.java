package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
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
    private final EventService eventService;

    public Review create(Review review) {
        checkUser(review.getUserId());
        checkFilm(review.getFilmId());

        Review reviewInReturningCondition = reviewStorage.create(review);

        Event event = Event.builder()
                .userId(reviewInReturningCondition.getUserId())
                .entityId(reviewInReturningCondition.getReviewId())
                .eventType("REVIEW")
                .operation("ADD")
                .build();
        eventService.addEvent(event);

        return reviewInReturningCondition;
    }

    public Review update(Review review) {
        checkReview(review.getReviewId());

        Review reviewInReturningCondition = reviewStorage.update(review);

        Event event = Event.builder()
                .userId(reviewInReturningCondition.getUserId())
                .entityId(reviewInReturningCondition.getReviewId())
                .eventType("REVIEW")
                .operation("UPDATE")
                .build();
        eventService.addEvent(event);

        return reviewInReturningCondition;
    }

    public boolean remove(int id) {
        checkReview(id);

        Review review = getById(id);

        Event event = Event.builder()
                .userId(review.getUserId())
                .entityId(review.getReviewId())
                .eventType("REVIEW")
                .operation("REMOVE")
                .build();

        boolean wasRemoved = reviewStorage.remove(id);

        eventService.addEvent(event);

        return wasRemoved;
    }

    public Review getById(int id) {
        checkReview(id);
        return reviewStorage.getById(id);
    }

    public List<Review> getAllOrByFilmId(Optional<Integer> oFilmId, Optional<Integer> oCount) {
        int filmId = oFilmId.orElse(0);
        int count = oCount.orElse(10);
        if (filmId == 0) {
            return reviewStorage.get(count);
        } else {
            checkFilm(filmId);
            return reviewStorage.getByFilmId(filmId, count);
        }
    }

    public Review setLike(int id, int userId) {
        check(id, userId);
        return reviewStorage.setLike(id, userId);
    }

    public Review setDislike(int id, int userId) {
        check(id, userId);
        return reviewStorage.setDislike(id, userId);
    }

    public boolean removeLike(int id, int userId) {
        check(id, userId);
        return reviewStorage.removeLike(id, userId);
    }

    public boolean removeDislike(int id, int userId) {
        check(id, userId);
        return reviewStorage.removeDislike(id, userId);
    }

    private void checkReview(int id) {
        if (!reviewStorage.isExists(id)) {
            throw new NotFoundException(String.format("Отзыв с ID: '%s' не найден", id));
        }
    }

    private void checkUser(int userId) {
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID: '%s' не найден", userId));
        }
    }

    private void checkFilm(int filmId) {
        if (!filmStorage.isExists(filmId)) {
            throw new NotFoundException(String.format("Фильм с ID: '%s' не найден", filmId));
        }
    }

    private void check(int id, int userId) {
        checkUser(userId);
        checkReview(id);
    }
}
