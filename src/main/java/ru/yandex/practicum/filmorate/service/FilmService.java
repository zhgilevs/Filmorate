package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmService {

    private static final String FILM_NOT_FOUND = "Фильм с ID: '%s' не найден";
    private static final String USER_NOT_FOUND = "Пользователь с ID: '%s' не найден";
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }
}