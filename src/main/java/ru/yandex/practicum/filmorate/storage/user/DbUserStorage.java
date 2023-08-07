package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.DbFilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("DbUserStorage")
@Primary
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final JdbcOperations jdbcTemplate;

    private final DbFilmStorage dbFilmStorage;

    @Override
    public List<User> getAll() {
        String sqlQuery = "SELECT * FROM USERS";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO USERS (NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();
        String sqlQuery = "UPDATE USERS SET NAME=?, LOGIN=?, EMAIL=?, BIRTHDAY=? WHERE ID=?";
        jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                id);
        return getById(id);
    }

    @Override
    public User getById(int id) {
        String sqlQuery = "SELECT * FROM USERS WHERE ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            User user = User.builder()
                    .id(userRows.getInt("ID"))
                    .name(userRows.getString("NAME"))
                    .login(userRows.getString("LOGIN"))
                    .email(userRows.getString("EMAIL"))
                    .birthday(Objects.requireNonNull(userRows.getDate("BIRTHDAY")).toLocalDate())
                    .build();
            user.setFriends(new HashSet<>(getFriendsIdByUserId(id)));
            return user;
        } else {
            throw new NotFoundException("Пользователь с ID: '" + id + "' не найден");
        }
    }

    @Override
    public User addToFriends(int userId, int friendId) {
        boolean isFriendExists = false;
        String sqlQuery = "SELECT * FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, friendId, userId);
        if (userRows.next()) {
            isFriendExists = true;
        }
        if (!isFriendExists) {
            sqlQuery = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID) values (?, ?)";
            jdbcTemplate.update(sqlQuery, userId, friendId);
        }
        return getById(userId);
    }

    @Override
    public User removeFromFriends(int userId, int friendId) {
        String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        return getById(userId);
    }

    @Override
    public List<User> getFriendList(int id) {
        String sqlQuery = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getFriend(rs), id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        List<User> commonFriends = new ArrayList<>();
        String sqlQuery = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=? " +
                "AND FRIEND_ID IN (SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?)";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id, otherId);
        if (userRows.next()) {
            commonFriends.add(getById(userRows.getInt("FRIEND_ID")));
        }
        return commonFriends;
    }

    @Override
    public boolean isExists(int id) {
        String sqlQuery = "SELECT ID FROM USERS WHERE ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            int idFromUsers = userRows.getInt("ID");
            return idFromUsers == id;
        }
        return false;
    }

    @Override
    public List<Film> getRecommendedFilmForUser(int targetUserId) {
        String sql = "SELECT DISTINCT f.id, f.name, f.description, f.duration, f.release_date, " +
                "f.mpa_id, m.name AS mpa_name, m.description AS mpa_description, " +
                "g.genre_id, g.name AS genre_name " +
                "FROM films f " +
                "INNER JOIN likes l ON f.id = l.film_id " +
                "INNER JOIN ( " +
                "  SELECT user_id FROM likes WHERE film_id IN " +
                "  (SELECT film_id FROM likes WHERE user_id = ?) " +
                "  AND user_id <> ? " +
                ") u ON l.user_id = u.user_id " +
                "LEFT JOIN likes tl ON tl.film_id = f.id AND tl.user_id = ? " +
                "LEFT JOIN mpas m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE tl.user_id IS NULL";

        Map<Integer, Film> filmMap = new HashMap<>();

        jdbcTemplate.query(sql, (ResultSet rs) -> {
            try {
                int filmId = rs.getInt("id");

                if (!filmMap.containsKey(filmId)) {
                    Film film = dbFilmStorage.makeFilm(rs);

                    filmMap.put(filmId, film);
                }

                Film film = filmMap.get(filmId);

                int genreId = rs.getInt("genre_id");
                String genreName = rs.getString("genre_name");
                if (genreId != 0 && genreName != null) {
                    Genre genre = new Genre(genreId, genreName);
                    film.getGenres().add(genre);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Ошибка обработки ResultSet", e);
            }
        }, targetUserId, targetUserId, targetUserId);

        return new ArrayList<>(filmMap.values());
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) {
        try {
            return User.builder()
                    .id(resultSet.getInt("ID"))
                    .name(resultSet.getString("NAME"))
                    .login(resultSet.getString("LOGIN"))
                    .email(resultSet.getString("EMAIL"))
                    .birthday(resultSet.getDate("BIRTHDAY").toLocalDate())
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения пользователя");
        }
    }

    private User getFriend(ResultSet rs) {
        try {
            return getById(rs.getInt("FRIEND_ID"));
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения друга из база");
        }
    }

    private List<Integer> getFriendsIdByUserId(int id) {
        String sqlQuery = "SELECT FRIEND_ID from FRIENDS where USER_ID=?";
        return (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getFriendId(rs), id));
    }

    private Integer getFriendId(ResultSet rs) {
        try {
            return rs.getInt("FRIEND_ID");
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения друга из база");
        }
    }

    public User deleteUserById(int id) {
        User user = getById(id);
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, user.getId());
        return user;
    }
}