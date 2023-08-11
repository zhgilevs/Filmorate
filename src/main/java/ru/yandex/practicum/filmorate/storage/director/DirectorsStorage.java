package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DirectorsStorage {
    Director create(Director director);

    Director delete(Director director);

    Director update(Director director);

    List<Director> getAll();

    Optional<Director> getDirectorById(int id);

    void deleteDirectorsFrom(Film film);

    void saveDirectorsFor(Film film);

    List<Director> getFilmDirectors(Film film);

    Map<Integer, HashSet<Director>> getDirectorsForFilms(List<Film> films);
}
