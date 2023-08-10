package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("InMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate CINEMA_STARTING_POINT = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap<>();
    private int globalId = 0;

    @Override
    public List<Film> getAll() {
        log.info("Запрошен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        validateReleaseDate(film);
        int id = ++globalId;
        film.setId(id);
        films.put(id, film);
        log.info("Фильм: '{}' создан", film);
        return film;
    }

    @Override
    public Film update(Film updatedFilm) {
        validateReleaseDate(updatedFilm);
        int id = updatedFilm.getId();
        if (films.containsKey(id)) {
            films.put(id, updatedFilm);
            log.info("Фильм с ID: '{}' обновлен", id);
            return updatedFilm;
        } else {
            throw new NotFoundException("Фильм с ID: '" + id + "' не найден");
        }
    }

    @Override
    public Film getById(int id) {
        log.info("Запрошен фильм с ID: '{}'", id);
        return films.get(id);
    }

    @Override
    public Film addLike(int id, int userId) {
        Film film = films.get(id);
        film.getLikes().add(userId);
        log.info("Пользователь с ID: '{}' поставил лайк фильму с ID: '{}'", userId, id);
        return film;
    }

    @Override
    public Film removeLike(int id, int userId) {
        Film film = films.get(id);
        film.getLikes().remove(userId);
        log.info("Пользователь с ID: '{}' удалил лайк с фильма с ID: '{}'", userId, id);
        return film;
    }

    @Override
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        log.info("Запрошен список {} самых популярных фильмов", count);
        return films.values().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isExists(int id) {
        return films.containsKey(id);
    }

    @Override
    public List<Film> getFilmsByDirector(Director director) {
        throw new NotImplementedException();
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть передана в запросе");
        }
        if (film.getReleaseDate().isBefore(CINEMA_STARTING_POINT)) {
            throw new ValidationException("Дата релиза должна быть позже 1895-12-28");
        }
    }

    @Override
    public Film deleteFilmById(int id) {
        return getById(id);
    }

    @Override
    public List<Film> searchFilms(boolean searchByTitle, boolean searchByDirector, String query) {
        return new ArrayList<>();
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        return new ArrayList<>();
    }
}
