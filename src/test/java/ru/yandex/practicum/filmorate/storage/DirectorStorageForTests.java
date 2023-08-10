package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorsStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DirectorStorageForTests implements DirectorsStorage {
    @Override
    public Director create(Director director) {
        return null;
    }

    @Override
    public Director delete(Director director) {
        return null;
    }

    @Override
    public Director update(Director director) {
        return null;
    }

    @Override
    public List<Director> getAll() {
        return null;
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        return Optional.empty();
    }

    @Override
    public void deleteDirectorsFrom(Film film) {

    }

    @Override
    public void saveDirectorsFor(Film film) {

    }

    @Override
    public List<Director> getFilmDirectors(Film film) {
        return null;
    }

    @Override
    public Map<Integer, HashSet<Director>> getDirectorsForFilms(List<Film> films) {
        return null;
    }
}
