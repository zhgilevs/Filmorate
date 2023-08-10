package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.StorageUtils;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DbDirectorsStorage implements DirectorsStorage {
    private final NamedParameterJdbcOperations jdbcOperations;

    private static MapSqlParameterSource getMapToQuery(Director director) {
        MapSqlParameterSource map = new MapSqlParameterSource();

        map.addValue("name", director.getName());
        map.addValue("id", director.getId());
        return map;
    }

    @Override
    public Director create(Director director) {
        final String sqlQuery = "insert into DIRECTORS (NAME) " + // в подготовленных тестах уже приходит id
                "values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(sqlQuery, getMapToQuery(director), keyHolder);

        int createdDirectorId = keyHolder.getKey().intValue();

        director.setId(createdDirectorId);
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

    @Override
    public List<Director> getAll() {
        final String sqlQuery = "select * " +
                "from DIRECTORS";

        return jdbcOperations.query(sqlQuery, StorageUtils::directorMapRow);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        final String sqlQuery = "select * " +
                "from DIRECTORS " +
                "where DIRECTOR_ID = :id";
        final List<Director> directors = jdbcOperations.query(sqlQuery, Map.of("id", id), StorageUtils::directorMapRow);
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
        return jdbcOperations.query(sqlQuery, Map.of("filmId", film.getId()), StorageUtils::directorMapRow);
    }

    @Override
    public Map<Integer, HashSet<Director>> getDirectorsForFilms(List<Film> films) {
        Map<Integer, HashSet<Director>> filmsAndDirectors = films.stream()
                .collect(Collectors.toMap(film -> film.getId(), film -> new HashSet<Director>()));
        final String sqlQuery = "select * " +
                "from FILMS_AND_DIRECTORS " +
                "left join DIRECTORS D on D.DIRECTOR_ID = FILMS_AND_DIRECTORS.DIRECTOR_ID " +
                "where FILM_ID IN (:filmsIds)";
        SqlRowSet sqlRowSet = jdbcOperations.queryForRowSet(sqlQuery, Map.of("filmsIds", new ArrayList(filmsAndDirectors.keySet())));

        while (sqlRowSet.next()) {
            int filmId = sqlRowSet.getInt("FILM_ID");
            int directorId = sqlRowSet.getInt("DIRECTOR_ID");
            String directorName = sqlRowSet.getString("NAME");
            Director director = new Director();

            director.setName(directorName);
            director.setId(directorId);
            filmsAndDirectors.get(filmId).add(director);
            System.out.println("1.6");
        }
        return filmsAndDirectors;
    }
}
