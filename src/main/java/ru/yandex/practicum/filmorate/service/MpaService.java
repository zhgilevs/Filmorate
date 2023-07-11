package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {

    private final MpaStorage storage;

    @Autowired
    public MpaService(MpaStorage storage) {
        this.storage = storage;
    }

    public List<Mpa> getAll() {
        return storage.getAll();
    }

    public Mpa getById(int id) {
        Mpa mpa = storage.getById(id);
        if (mpa == null) {
            throw new NotFoundException("Ретинг фильма не найден");
        } else {
            return mpa;
        }
    }
}
