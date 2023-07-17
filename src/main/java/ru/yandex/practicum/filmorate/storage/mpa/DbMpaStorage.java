package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.StorageUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DbMpaStorage implements MpaStorage {

    private final JdbcOperations jdbcTemplate;

    @Override
    public List<Mpa> getAll() {
        String query = "SELECT * FROM MPAS;";
        return jdbcTemplate.query(query, (rs, rowNum) -> makeMpa(rs));
    }

    @Override
    public Mpa getById(int id) {
        return StorageUtils.makeMpa(jdbcTemplate, id);
    }

    private Mpa makeMpa(ResultSet rs) {
        try {
            return StorageUtils.makeMpa(jdbcTemplate, rs.getInt("MPA_ID"));
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения рейтинга фильма");
        }
    }
}
