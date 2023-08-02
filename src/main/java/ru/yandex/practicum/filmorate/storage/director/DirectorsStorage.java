package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.director.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorsStorage {
    Director create(Director director);

    Director delete(Director director);

    Director update(Director director);

    List<Director> getAll();

    Optional<Director> getDirectorById(int id);

}
