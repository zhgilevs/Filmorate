package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component("DbReviewStorage")
@Primary
@RequiredArgsConstructor
public class DbReviewStorage implements ReviewStorage {

    private final JdbcOperations jdbcTemplate;

    @Override
    public Review create(Review review) {
        String reviewQuery = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID) VALUES (?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(reviewQuery, new String[]{"REVIEW_ID"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            return stmt;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        int reviewId = review.getReviewId();
        String sqlQuery = "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=? WHERE REVIEW_ID=?;";
        jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                reviewId);
        return getById(reviewId);
    }

    @Override
    public boolean remove(int id) {
        int reviewId = getById(id).getReviewId();
        String sqlQuery = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
        return !isExists(reviewId);
    }

    @Override
    public Review getById(int id) {
        String sqlQuery = "SELECT * FROM REVIEWS WHERE REVIEW_ID=?";
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (reviewRows.next()) {
            return Review.builder()
                    .reviewId(reviewRows.getInt("REVIEW_ID"))
                    .content(reviewRows.getString("CONTENT"))
                    .isPositive(reviewRows.getBoolean("IS_POSITIVE"))
                    .userId(reviewRows.getInt("USER_ID"))
                    .filmId(reviewRows.getInt("FILM_ID"))
                    .useful(reviewRows.getInt("USEFUL"))
                    .build();
        } else {
            throw new NotFoundException("Отзыв с ID: '" + id + "' не найден");
        }
    }

    @Override
    public List<Review> get(int count) {
        String query = "SELECT * FROM REVIEWS ORDER BY USEFUL DESC LIMIT ?;";
        return jdbcTemplate.query(query, (rs, rowNum) -> makeReview(rs), count);
    }

    @Override
    public List<Review> getByFilmId(int filmId, int count) {
        String query = "SELECT * FROM REVIEWS WHERE FILM_ID=? ORDER BY USEFUL DESC LIMIT ?;";
        return jdbcTemplate.query(query, (rs, rowNum) -> makeReview(rs), filmId, count);
    }

    @Override
    public Review setLike(int reviewId, int userId) {
        if (isReactionExists(reviewId, userId)) {
            setFlagIfExists(true, reviewId, userId);
            int useful = getUseful(reviewId);
            useful += 1;
            setUseful(useful, reviewId);
        } else {
            setFlagIfNotExists(true, reviewId, userId);
            int useful = getUseful(reviewId);
            useful += 1;
            setUseful(useful, reviewId);
        }
        return getById(reviewId);
    }

    @Override
    public Review setDislike(int reviewId, int userId) {
        if (isReactionExists(reviewId, userId)) {
            setFlagIfExists(false, reviewId, userId);
            int useful = getUseful(reviewId);
            useful -= 1;
            setUseful(useful, reviewId);
        } else {
            setFlagIfNotExists(false, reviewId, userId);
            int useful = getUseful(reviewId);
            useful -= 1;
            setUseful(useful, reviewId);
        }
        return getById(reviewId);
    }

    @Override
    public boolean removeLike(int reviewId, int userId) {
        if (isReactionExists(reviewId, userId)) {
            if (getFlag(reviewId, userId)) {
                setFlagIfExists(null, reviewId, userId);
                int useful = getUseful(reviewId);
                useful -= 1;
                setUseful(useful, reviewId);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean removeDislike(int reviewId, int userId) {
        if (isReactionExists(reviewId, userId)) {
            if (!getFlag(reviewId, userId)) {
                setFlagIfExists(null, reviewId, userId);
                int useful = getUseful(reviewId);
                useful += 1;
                setUseful(useful, reviewId);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isExists(int id) {
        String sqlQuery = "SELECT REVIEW_ID FROM REVIEWS WHERE REVIEW_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            int idFromReviews = userRows.getInt("REVIEW_ID");
            return idFromReviews == id;
        }
        return false;
    }

    private Review makeReview(ResultSet rs) {
        try {
            return Review.builder()
                    .reviewId(rs.getInt("REVIEW_ID"))
                    .content(rs.getString("CONTENT"))
                    .isPositive(rs.getBoolean("IS_POSITIVE"))
                    .userId(rs.getInt("USER_ID"))
                    .filmId(rs.getInt("FILM_ID"))
                    .useful(rs.getInt("USEFUL"))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения отзыва");
        }
    }

    private Boolean getFlag(int reviewId, int userId) {
        String sqlQuery = "SELECT LIKE_FLAG FROM REVIEWS_LIKES WHERE REVIEW_ID=? AND USER_ID=?";
        SqlRowSet usefulRows = jdbcTemplate.queryForRowSet(sqlQuery, reviewId, userId);
        if (usefulRows.next()) {
            return usefulRows.getBoolean("LIKE_FLAG");
        } else {
            throw new DatabaseException("Не удалось получить статус лайка/дизлайка");
        }
    }

    private void setFlagIfNotExists(Boolean flag, int reviewId, int userId) {
        String sqlQuery = "INSERT INTO REVIEWS_LIKES (USER_ID, REVIEW_ID, LIKE_FLAG) VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, userId, reviewId, flag);
    }

    private void setFlagIfExists(Boolean flag, int reviewId, int userId) {
        String sqlQuery = "UPDATE REVIEWS_LIKES SET LIKE_FLAG=? WHERE REVIEW_ID=? AND USER_ID=?;";
        jdbcTemplate.update(sqlQuery, flag, reviewId, userId);
    }

    private int getUseful(int reviewId) {
        String sqlQuery = "SELECT USEFUL FROM REVIEWS WHERE REVIEW_ID=?";
        SqlRowSet usefulRows = jdbcTemplate.queryForRowSet(sqlQuery, reviewId);
        if (usefulRows.next()) {
            return usefulRows.getInt("USEFUL");
        } else {
            throw new DatabaseException("Не удалось получить рейтинг отзыва");
        }
    }

    private void setUseful(int useful, int reviewId) {
        String sqlQuery = "UPDATE REVIEWS SET USEFUL=? WHERE REVIEW_ID=?;";
        jdbcTemplate.update(sqlQuery, useful, reviewId);
    }

    private boolean isReactionExists(int reviewId, int userId) {
        String sqlQuery = "SELECT USER_ID FROM REVIEWS_LIKES WHERE REVIEW_ID=? AND USER_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, reviewId, userId);
        if (userRows.next()) {
            int userIdFromReviewsLikes = userRows.getInt("USER_ID");
            return userIdFromReviewsLikes == reviewId;
        }
        return false;
    }
}