package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.director.Director;

import java.util.List;

public interface FilmStorage {
    List<Film> getAll();

    List<Film> getPopular(int count, Integer genreId, Integer year);

    Film create(Film film);

    Film update(Film film);

    Film getById(int id);

    Film addLike(int id, int userId);

    Film removeLike(int id, int userId);

    boolean isExists(int id);

    Film deleteFilmById(int id);

    List<Film> getFilmsByDirector(Director director);

    List<Film> searchFilms(boolean searchByTitle, boolean searchByDirector, String query);
}
