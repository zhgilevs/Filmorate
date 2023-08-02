package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmService {

    private static final LocalDate CINEMA_STARTING_POINT = LocalDate.of(1895, 12, 28);
    private static final String FILM_NOT_FOUND = "Фильм с ID: '%s' не найден";
    private static final String USER_NOT_FOUND = "Пользователь с ID: '%s' не найден";
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("DbFilmStorage") FilmStorage filmStorage,
                       @Qualifier("DbUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film getById(int id) {
        if (filmStorage.isExists(id)) {
            return filmStorage.getById(id);
        } else {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
    }

    public Film addLike(int id, int userId) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }
        return filmStorage.addLike(id, userId);
    }

    public Film removeLike(int id, int userId) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }
        return filmStorage.removeLike(id, userId);
    }

    public List<Film> getPopular(int count) {
        if (filmStorage.getAll().isEmpty()) {
            throw new NotFoundException("Список фильмов пуст");
        } else {
            return filmStorage.getPopular(count);
        }
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateReleaseDate(film);
        int id = film.getId();
        if (filmStorage.isExists(id)) {
            return filmStorage.update(film);
        } else {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть передана в запросе");
        }
        if (film.getReleaseDate().isBefore(CINEMA_STARTING_POINT)) {
            throw new ValidationException("Дата релиза должна быть позже 1895-12-28");
        }
    }

    public Film deleteFilmById(int id) {
        if (filmStorage.isExists(id)) {
            return filmStorage.deleteFilmById(id);
        } else {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
    }
}