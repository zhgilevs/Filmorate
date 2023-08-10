package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.StorageUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("DbFilmStorage")
@Primary
@RequiredArgsConstructor
public class DbFilmStorage implements FilmStorage {

    private final JdbcOperations jdbcTemplate;
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public List<Film> getAll() {
        String query = "SELECT * FROM FILMS;";

        return jdbcTemplate.query(query, (rs, rowNum) -> makeFilm(rs));
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
        saveGenresFrom(film);
        setGenresTo(film);
        setMpaTo(film);
        return film;
    }

    private void setMpaTo(Film film) {
        Mpa mpa = getMpaById(film.getMpa().getId());
        film.setMpa(mpa);
    }

    private void setGenresTo(Film film) {
        Map<Integer, String> genres = getGenresById(film.getId()).stream()
                .collect(Collectors.toMap(Genre::getId, Genre::getName));
        for (Genre genre : film.getGenres()) {
            Integer genreId = genre.getId();
            genre.setName(genres.get(genreId));
        }
    }

    private void saveGenresFrom(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlQuery = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) values (?, ?);";

            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
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
            film.setMpa(getMpaById(film.getMpa().getId()));
            return film;
        }
        return null;
    }

    @Override
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        String sqlQuery =
                "SELECT F.ID,F.NAME,F.DESCRIPTION,F.DURATION,F.RELEASE_DATE,F.MPA_ID " +
                        "FROM FILMS F " +
                        "LEFT JOIN LIKES L ON F.ID = L.FILM_ID " +
                        "GROUP BY F.ID " +
                        "ORDER BY COUNT(L.USER_ID) DESC " +
                        "LIMIT ?;";
        if (Objects.nonNull(genreId) && Objects.nonNull(year)) {
            sqlQuery =
                    "SELECT F.ID,F.NAME,F.DESCRIPTION,F.DURATION,F.RELEASE_DATE,F.MPA_ID " +
                            "FROM FILMS F " +
                            "LEFT JOIN LIKES L ON F.ID = L.FILM_ID " +
                            "WHERE EXTRACT(YEAR FROM F.RELEASE_DATE) = ? AND F.ID IN " +
                            "(SELECT FILM_ID FROM FILM_GENRES FG WHERE FG.GENRE_ID = ?) " +
                            "GROUP BY F.ID " +
                            "ORDER BY COUNT(L.USER_ID) DESC " +
                            "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, genreId, count);
        }
        if (Objects.nonNull(genreId) || Objects.nonNull(year)) {
            if (Objects.nonNull(genreId)) {
                sqlQuery =
                        "SELECT F.ID,F.NAME,F.DESCRIPTION,F.DURATION,F.RELEASE_DATE,F.MPA_ID " +
                                "FROM FILMS F " +
                                "LEFT JOIN LIKES L ON F.ID = L.FILM_ID " +
                                "WHERE F.ID IN " +
                                "(SELECT FILM_ID FROM FILM_GENRES FG WHERE FG.GENRE_ID = ?) " +
                                "GROUP BY F.ID " +
                                "ORDER BY COUNT(L.USER_ID) DESC " +
                                "LIMIT ?;";
                return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), genreId, count);
            }
            sqlQuery =
                    "SELECT F.ID,F.NAME,F.DESCRIPTION,F.DURATION,F.RELEASE_DATE,F.MPA_ID " +
                            "FROM FILMS F " +
                            "LEFT JOIN LIKES L ON F.ID = L.FILM_ID " +
                            "WHERE EXTRACT(YEAR FROM F.RELEASE_DATE) = ? " +
                            "GROUP BY F.ID " +
                            "ORDER BY COUNT(L.USER_ID) DESC " +
                            "LIMIT ?;";
            return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), year, count);
        }
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), count);
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

    @Override
    public List<Film> getFilmsByDirector(Director director) {
        String query = "select * " +
                "from FILMS " +
                "right join FILMS_AND_DIRECTORS FAD on FILMS.ID = FAD.FILM_ID " +
                "where FAD.DIRECTOR_ID = :directorId";

        return jdbcOperations.query(query, Map.of("directorId", director.getId()), (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public List<Film> searchFilms(boolean searchByTitle, boolean searchByDirector, String query) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT films.ID, films.NAME, films.DESCRIPTION, films.DURATION, films.RELEASE_DATE, ");
        stringBuilder.append("films.MPA_ID, COUNT(likes.user_id) FROM films ");
        stringBuilder.append("LEFT JOIN likes ON films.id=likes.film_id ");
        if (searchByTitle && searchByDirector) {
            stringBuilder.append("LEFT JOIN films_and_directors ON films.id=films_and_directors.film_id ");
            stringBuilder.append("LEFT JOIN directors ON films_and_directors.director_id=directors.director_id ");
            stringBuilder.append("WHERE UPPER(films.name) LIKE UPPER('%" + query + "%') ");
            stringBuilder.append("OR UPPER(directors.name) LIKE UPPER('%" + query + "%') ");
        } else if (searchByTitle) {
            stringBuilder.append("WHERE UPPER(films.name) LIKE UPPER('%" + query + "%') ");
        } else if (searchByDirector) {
            stringBuilder.append("LEFT JOIN films_and_directors ON films.id=films_and_directors.film_id ");
            stringBuilder.append("LEFT JOIN directors ON films_and_directors.director_id=directors.director_id ");
            stringBuilder.append("WHERE UPPER(directors.name) LIKE UPPER('%" + query + "%') ");
        }
        stringBuilder.append("GROUP BY films.id ");
        stringBuilder.append("ORDER BY COUNT(likes.user_id) DESC, films.id DESC");

        return jdbcTemplate.query(stringBuilder.toString(), (rs, rowNum) -> makeFilm(rs));
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

    public Film makeFilm(ResultSet rs) {
        try {
            Film film = Film.builder()
                    .id(rs.getInt("ID"))
                    .name(rs.getString("NAME"))
                    .description(rs.getString("DESCRIPTION"))
                    .duration(rs.getInt("DURATION"))
                    .releaseDate(rs.getDate("RELEASE_DATE").toLocalDate())
                    .mpa(getMpaById(rs.getInt("MPA_ID")))
                    .build();
            film.setGenres(getGenresById(film.getId()));
            film.setLikes(getLikesById(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения фильма");
        }
    }

    private HashSet<Director> getDirectorsByFilmId(int filmId) {
        String sqlQuery = "SELECT director_id, name FROM directors " +
                "WHERE director_id IN (SELECT director_id FROM films_and_directors WHERE film_id=?)";
        return new HashSet<>(jdbcTemplate.query(sqlQuery, StorageUtils::directorMapRow, filmId));
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

    public Film deleteFilmById(int id) {
        Film film = getById(id);
        String sql = "DELETE FROM films WHERE id=?";
        jdbcTemplate.update(sql, id);
        return film;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT f.ID, f.MPA_ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION " +
                "FROM FILMS AS f " +
                "INNER JOIN LIKES AS l1 ON f.ID = l1.FILM_ID AND l1.USER_ID = ? " +
                "INNER JOIN LIKES as l2 ON l1.FILM_ID = l2.FILM_ID AND l2.USER_ID = ? " +
                "LEFT JOIN LIKES AS l ON l.FILM_ID = f.ID " +
                "GROUP BY f.ID";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), userId, friendId);
    }
}