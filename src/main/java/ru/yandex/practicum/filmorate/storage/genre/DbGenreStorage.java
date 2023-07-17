package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {

    private final JdbcOperations jdbcTemplate;

    @Override
    public List<Genre> getAll() {
        String query = "SELECT * FROM GENRES;";
        return jdbcTemplate.query(query, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Genre getById(int id) {
        String query = "SELECT * FROM GENRES WHERE GENRE_ID=?;";
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(query, id);
        if (genreRows.next()) {
            return Genre.builder()
                    .id(genreRows.getInt("GENRE_ID"))
                    .name(genreRows.getString("NAME"))
                    .build();
        }
        return null;
    }

    private Genre makeGenre(ResultSet rs) {
        try {
            return Genre.builder()
                    .id(rs.getInt("GENRE_ID"))
                    .name(rs.getString("NAME"))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения жанра фильма");
        }
    }
}
