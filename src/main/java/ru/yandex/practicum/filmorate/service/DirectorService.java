package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorsStorage;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorsStorage directorsStorage;

    public Director create(Director director) {
        return directorsStorage.create(director);
    }

    public Director update(Director director) {
        if (!isDirectorExists(director)) {
            throw new ValidationException("Неизвестный режиссер");
        }
        return directorsStorage.update(director);
    }

    public List<Director> getAll() {
        return new ArrayList<Director>();
    }

    public Director delete(Director director) {
        return null;
    }

    public Director getById(int id) {
        return null;
    }

    private boolean isDirectorExists(Director director) {
        return getAll().contains(director);
    }
}
