package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorsStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorsStorage directorsStorage;

    public Director create(Director director) {
        return directorsStorage.create(director);
    }

    public Director update(Director director) {
        checksOnUpdateDirectors(director);
        return directorsStorage.update(director);
    }

    private void checksOnUpdateDirectors(Director director) {
        getById(director.getId());
    }

    public List<Director> getAll() {
        return directorsStorage.getAll();
    }

    public Director delete(int id) {
        Director director = getById(id);
        return directorsStorage.delete(director);
    }

    public Director getById(int id) {
        final Director director = directorsStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Не найден режиссер с id = " + id));

        return director;
    }

    public void handleDirectorsWhenCreateAndUpdateFilm(Film film) {
        checkAndSetDirectorsTo(film);
        directorsStorage.deleteDirectorsFrom(film);
        directorsStorage.saveDirectorsFor(film);
    }

    public void handleDirectorsWhenGetFilm(Film film) {
        List<Director> directors = directorsStorage.getFilmDirectors(film);

        film.setDirectors(new HashSet<>(directors));
    }

    public void handleDirectorsWhenGetListFilms(List<Film> films) {
        Map<Integer, HashSet<Director>> filmsAndDirectors = directorsStorage.getDirectorsForFilms(films);
        films.stream()
                .peek(film -> film.setDirectors(filmsAndDirectors.get(film.getId())))
                .collect(Collectors.toList());
    }

    private void checkAndSetDirectorsTo(Film film) {
        /*
        если режиссеры не были указаны
         */
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            film.setDirectors(new HashSet<>());
            return;
        }
        /*
         * Хеш-таблица вида:
         * Ключ - id известного режиссера
         * Значение - имя известного режиссера, соответствующего id
         */
        Map<Integer, String> allDirectors = getAll().stream()
                .collect(Collectors.toMap(Director::getId, Director::getName));
        /*
           Список id режиссеров у пришедшего фильма
         */
        List<Integer> filmDirectorsIds = film.getDirectors().stream()
                .map(Director::getId)
                .collect(Collectors.toList());
        /*
           Множество, которое будет установлено в поле к фильму
         */
        HashSet<Director> directors = new HashSet<>();

        for (Integer directorId : filmDirectorsIds) {
            if (!allDirectors.containsKey(directorId))
                throw new NotFoundException("Не добавлен режиссер с id = " + directorId);

            Director director = new Director();

            director.setId(directorId);
            director.setName(allDirectors.get(directorId));
            directors.add(director);
        }
        film.setDirectors(directors);
    }
}
