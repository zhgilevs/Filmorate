package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.StorageUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component("DbFilmStorage")
@Primary
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {

    private final JdbcOperations jdbcTemplate;

    @Override
    public List<Film> getAll() {
        String query = "SELECT * FROM FILMS;";
        List<Film> films = jdbcTemplate.query(query, (rs, rowNum) -> makeFilm(rs));
        for (Film film : films) {
            film.setGenres(getGenresById(film.getId()));
            film.setLikes(getLikesById(film.getId()));
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        String filmQuery = "INSERT INTO FILMS (MPA_ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION) values (?, ?, ?, ?, ? );";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(filmQuery, new String[]{"ID"});
            stmt.setInt(1, film.getMpa().getId());
            stmt.setString(2, film.getName());
            stmt.setString(3, film.getDescription());
            stmt.setDate(4, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(5, film.getDuration());
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlQuery = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) values (?, ?);";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE FILMS SET NAME=?, DESCRIPTION=?, DURATION=?, RELEASE_DATE=?, MPA_ID=? WHERE ID=?;";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId());
        updateGenres(film);
        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        String sqlQuery = "SELECT * FROM FILMS WHERE ID=?;";
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (filmRows.next()) {
            Film film = Film.builder()
                    .id(filmRows.getInt("ID"))
                    .name(filmRows.getString("NAME"))
                    .description(filmRows.getString("DESCRIPTION"))
                    .duration(filmRows.getInt("DURATION"))
                    .releaseDate(Objects.requireNonNull(filmRows.getDate("RELEASE_DATE")).toLocalDate())
                    .mpa(getMpaById(filmRows.getInt("MPA_ID")))
                    .build();
            film.setGenres(getGenresById(id));
            film.setLikes(getLikesById(id));
            return film;
        }
        return null;
    }

    @Override
    public List<Film> getPopular(int count) {
        String sqlQuery =
                "SELECT F.ID,F.NAME,F.DESCRIPTION,F.DURATION,F.RELEASE_DATE,F.MPA_ID " +
                        "FROM FILMS F " +
                        "LEFT JOIN LIKES L ON F.ID = L.FILM_ID " +
                        "GROUP BY F.ID " +
                        "ORDER BY COUNT(L.USER_ID) DESC " +
                        "LIMIT ?;";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), count);
        for (Film film : films) {
            film.setGenres(getGenresById(film.getId()));
            film.setLikes(getLikesById(film.getId()));
        }
        return films;
    }

    @Override
    public Film addLike(int id, int userId) {
        boolean isLikeExists = false;
        String sqlQuery = "SELECT * FROM LIKES WHERE FILM_ID=? AND USER_ID=?;";
        SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlQuery, id, userId);
        if (rows.next()) {
            isLikeExists = true;
        }
        if (!isLikeExists) {
            sqlQuery = "INSERT INTO LIKES (USER_ID, FILM_ID) VALUES (?,?);";
            jdbcTemplate.update(sqlQuery, userId, id);
        }
        return getById(id);
    }

    @Override
    public Film removeLike(int id, int userId) {
        String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID=? AND USER_ID=?;";
        jdbcTemplate.update(sqlQuery, id, userId);
        return getById(id);
    }

    @Override
    public boolean isExists(int id) {
        String sqlQuery = "SELECT ID FROM FILMS WHERE ID=?;";
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (filmRows.next()) {
            int idFromFilms = filmRows.getInt("ID");
            return idFromFilms == id;
        }
        return false;
    }

    private Mpa getMpaById(int id) {
        return StorageUtils.makeMpa(jdbcTemplate, id);
    }

    private Set<Genre> getGenresById(int id) {
        String sqlQuery = "SELECT fg.GENRE_ID, g.NAME FROM FILM_GENRES AS fg " +
                "LEFT JOIN GENRES AS g ON g.GENRE_ID = fg.GENRE_ID WHERE FILM_ID=?;";
        List<Genre> genres = (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeGenre(rs), id));
        if (genres.isEmpty()) {
            return new HashSet<>();
        } else {
            return new HashSet<>(genres);
        }
    }

    private void updateGenres(Film film) {
        String sqlQuery = "DELETE FROM FILM_GENRES WHERE FILM_ID = ?;";
        jdbcTemplate.update(sqlQuery, film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> genres = new HashSet<>(film.getGenres());
            sqlQuery = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) VALUES (?, ? );";
            for (Genre genre : genres) {
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
    }

    private Set<Integer> getLikesById(int id) {
        String sqlQuery = "SELECT U.ID, U.NAME, U.LOGIN, U.EMAIL, U.BIRTHDAY " +
                "FROM USERS U " +
                "LEFT JOIN LIKES L ON U.ID = L.USER_ID " +
                "WHERE L.FILM_ID = ?;";
        List<User> users = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeUser(rs), id);
        if (users.isEmpty()) {
            return new HashSet<>();
        } else {
            Set<Integer> likes = new HashSet<>();
            for (User user : users) {
                likes.add(user.getId());
            }
            return likes;
        }
    }

    private Genre makeGenre(ResultSet rs) {
        try {
            return Genre.builder()
                    .id(rs.getInt("GENRE_ID"))
                    .name(rs.getString("NAME"))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения жанра");
        }
    }

    private Film makeFilm(ResultSet rs) {
        try {
            return Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .duration(rs.getInt("DURATION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .mpa(getMpaById(rs.getInt("MPA_ID")))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения фильма");
        }
    }

    private User makeUser(ResultSet rs) {
        try {
            return User.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("NAME"))
                    .login(rs.getString("LOGIN"))
                    .email(rs.getString("EMAIL"))
                    .birthday(rs.getDate("BIRTHDAY").toLocalDate())
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения пользователя");
        }
    }
}