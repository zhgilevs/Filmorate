package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.director.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DbDirectorsStorage implements DirectorsStorage {
    private final NamedParameterJdbcOperations jdbcOperations;

    @Override
    public Director create(Director director) {
        final String sqlQuery = "insert into DIRECTORS (NAME) " + // в подготовленных тестах уже приходит id
                "values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(sqlQuery, getMapToQuery(director), keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director delete(Director director) {
        deleteDirectorFromAllFilms(director);

        final String sqlQuery = "delete from DIRECTORS where DIRECTOR_ID = :id";

        jdbcOperations.update(sqlQuery, Map.of("id", director.getId()));
        return director;
    }

    private void deleteDirectorFromAllFilms(Director director) {
        final String sqlQuery = "delete from FILMS_AND_DIRECTORS where DIRECTOR_ID = :id";

        jdbcOperations.update(sqlQuery, Map.of("id", director.getId()));
    }

    @Override
    public Director update(Director director) {
        final String sqlQuery = "update DIRECTORS " +
                "set NAME = :name " +
                "where DIRECTOR_ID = :id";

        jdbcOperations.update(sqlQuery, getMapToQuery(director));
        return director;
    }

    private static MapSqlParameterSource getMapToQuery(Director director) {
        MapSqlParameterSource map = new MapSqlParameterSource();

        map.addValue("name", director.getName());
        map.addValue("id", director.getId());
        return map;
    }

    @Override
    public List<Director> getAll() {
        final String sqlQuery = "select * " +
                "from DIRECTORS";

        return jdbcOperations.query(sqlQuery, new DirectorRowMapper());
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        final String sqlQuery = "select * " +
                "from DIRECTORS " +
                "where DIRECTOR_ID = :id";
        final List<Director> directors = jdbcOperations.query(sqlQuery, Map.of("id", id), new DirectorRowMapper());
        return directors.size() == 0 ? Optional.empty() : Optional.of(directors.get(0));
    }

    @Override
    public void deleteDirectorsFrom(Film film) {
        final String sqlQuery = "delete from FILMS_AND_DIRECTORS " +
                "where FILM_ID = :filmId";

        jdbcOperations.update(sqlQuery, Map.of("filmId", film.getId()));
    }

    @Override
    public void saveDirectorsFor(Film film) {
        Set<Director> directors = film.getDirectors();

        if (directors != null || !directors.isEmpty()) {
            final String sqlGenresQuery = "insert into FILMS_AND_DIRECTORS values (:filmId, :directorId)";
            int i = 0;
            SqlParameterSource[] batchParams = new SqlParameterSource[directors.size()];

            for (Director director : directors) {
                MapSqlParameterSource params = new MapSqlParameterSource()
                        .addValue("directorId", director.getId())
                        .addValue("filmId", film.getId());
                batchParams[i++] = params;
            }
            jdbcOperations.batchUpdate(sqlGenresQuery, batchParams);
        }
    }

    @Override
    public List<Director> getFilmDirectors(Film film) {
        final String sqlQuery = "select * " +
                "from FILMS_AND_DIRECTORS " +
                "left join DIRECTORS on FILMS_AND_DIRECTORS.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID " +
                "where FILM_ID = :filmId";

        return jdbcOperations.query(sqlQuery, Map.of("filmId", film.getId()), new DirectorRowMapper());
    }

    @Override
    public Map<Integer, Set<Director>> getDirectorsForFilms(List<Film> films) {
        Map<Integer, Set<Director>> filmsAndDirectors = films.stream()
                .collect(Collectors.toMap(film -> film.getId(), film -> new HashSet<Director>()));
        final String sqlQuery = "select * " +
                "from FILMS_AND_DIRECTORS " +
                "left join DIRECTORS D on D.DIRECTOR_ID = FILMS_AND_DIRECTORS.DIRECTOR_ID";
        SqlRowSet sqlRowSet = jdbcOperations.queryForRowSet(sqlQuery, Map.of());

        while (sqlRowSet.next()) {
            int filmId = sqlRowSet.getInt("FILM_ID");
            int directorId = sqlRowSet.getInt("DIRECTOR_ID");
            String directorName = sqlRowSet.getString("NAME");
            Director director = new Director();

            director.setName(directorName);
            director.setId(directorId);
            filmsAndDirectors.get(filmId).add(director);
        }
        return filmsAndDirectors;
    }

    private final static class DirectorRowMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            Director director = new Director();

            director.setId(rs.getInt("DIRECTOR_ID"));
            director.setName(rs.getString("NAME"));
            return director;
        }
    }
}
