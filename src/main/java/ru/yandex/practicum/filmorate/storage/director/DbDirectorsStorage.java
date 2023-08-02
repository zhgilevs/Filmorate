package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.director.Director;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbDirectorsStorage implements DirectorsStorage {
    private final NamedParameterJdbcOperations jdbcOperations;
    @Override
    public Director create(Director director) {
        String sqlQuery = "insert into DIRECTORS (NAME) " + // в подготовленных тестах уже приходит id
                "values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(sqlQuery, getMapToCreateAndUpdateQuery(director), keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director delete(Director director) {
        return null;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "update DIRECTORS " +
                "set NAME = :name " +
                "where DIRECTOR_ID = :directorId";

        jdbcOperations.update(sqlQuery, getMapToCreateAndUpdateQuery(director));
        return director;
    }

    private MapSqlParameterSource getMapToCreateAndUpdateQuery(Director director) {
        MapSqlParameterSource map = new MapSqlParameterSource();

        map.addValue("name", director.getName());
        map.addValue("id", director.getId());
        return map;
    }

    @Override
    public List<Director> getAll() {
        return null;
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        return Optional.empty();
    }
}
