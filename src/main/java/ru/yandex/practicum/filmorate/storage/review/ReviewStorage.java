package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    boolean remove(int id);

    Review getById(int id);

    List<Review> get(int count);

    List<Review> getByFilmId(int filmId, int count);

    Review setLike(int reviewId, int userId);

    Review setDislike(int reviewId, int userId);

    boolean removeLike(int reviewId, int userId);

    boolean removeDislike(int reviewId, int userId);

    boolean isExists(int id);
}
