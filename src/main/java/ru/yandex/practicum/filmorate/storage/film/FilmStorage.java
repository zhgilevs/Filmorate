package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> getAll();

    List<Film> getPopular(int count);

    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    Film addLike(int id, int userId);

    Film removeLike(int id, int userId);

    boolean isExists(int id);

    Film deleteFilmById(int id);
}
