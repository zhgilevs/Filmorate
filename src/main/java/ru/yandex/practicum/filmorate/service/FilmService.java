package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.director.Director;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_STARTING_POINT = LocalDate.of(1895, 12, 28);
    private static final String FILM_NOT_FOUND = "Фильм с ID: '%s' не найден";
    private static final String USER_NOT_FOUND = "Пользователь с ID: '%s' не найден";
    @Qualifier("DbFilmStorage")
    private final FilmStorage filmStorage;
    @Qualifier("DbUserStorage")
    private final UserStorage userStorage;
    private final EventService eventService;
    private final DirectorService directorService;

    public Film getById(int id) {
        if (filmStorage.isExists(id)) {
            Film film = filmStorage.getById(id);
            directorService.handleDirectorsWhenGetFilm(film);
            return film;
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

        Film filmInReturningCondition = filmStorage.addLike(id, userId);

        Event event = Event.builder()
                .userId(userId)
                .entityId(id)
                .eventType("LIKE")
                .operation("ADD")
                .build();
        eventService.addEvent(event);

        return filmInReturningCondition;
    }

    public Film removeLike(int id, int userId) {
        if (!filmStorage.isExists(id)) {
            throw new NotFoundException(String.format(FILM_NOT_FOUND, id));
        }
        if (!userStorage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }

        Film filmInReturningCondition = filmStorage.removeLike(id, userId);

        Event event = Event.builder()
                .userId(userId)
                .entityId(id)
                .eventType("LIKE")
                .operation("REMOVE")
                .build();
        eventService.addEvent(event);

        return filmInReturningCondition;
    }

    public List<Film> getPopular(int count) {
        if (filmStorage.getAll().isEmpty()) {
            throw new NotFoundException("Список фильмов пуст");
        } else {
            List<Film> films = filmStorage.getPopular(count);
            directorService.handleDirectorsWhenGetListFilms(films);
            return films;
        }
    }

    public List<Film> getAll() {
        List<Film> films = filmStorage.getAll();
        directorService.handleDirectorsWhenGetListFilms(films);
        return films;
    }

    public Film create(Film film) {
        validateReleaseDate(film);
        filmStorage.create(film);
        directorService.handleDirectorsWhenCreateAndUpdateFilm(film);
        return film;
    }

    public Film update(Film film) {
        validateReleaseDate(film);
        int id = film.getId();
        if (filmStorage.isExists(id)) {
            filmStorage.update(film);
            directorService.handleDirectorsWhenCreateAndUpdateFilm(film);
            return getById(film.getId());
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

    public List<Film> getFilmsByDirector(int directorId, String sort) {
        Director director = directorService.getById(directorId);
        List<Film> directorFilms = filmStorage.getFilmsByDirector(director);
        directorService.handleDirectorsWhenGetListFilms(directorFilms);
        if (sort.equals("year")) {
            directorFilms = directorFilms.stream()
                    .sorted(new Comparator<Film>() {
                        @Override
                        public int compare(Film o1, Film o2) {
                            return o1.getReleaseDate().getYear() - o2.getReleaseDate().getYear();
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            directorFilms = directorFilms.stream()
                    .sorted(new Comparator<Film>() {
                        @Override
                        public int compare(Film o1, Film o2) {
                            return o1.getLikes().size() - o2.getLikes().size();
                        }
                    })
                    .collect(Collectors.toList());
        }
        System.out.println(directorFilms);
        return directorFilms;
    }

    public List<Film> searchFilms(String query, List<String> by) {
        boolean searchByTitle = by.contains("title");
        boolean searchByDirector = by.contains("director");

        if (by.isEmpty() || by.size() > 2 || (!searchByTitle && !searchByDirector)) {
            throw new ValidationException("Неправельный запрос области поиска");
        }

        return filmStorage.searchFilms(searchByTitle, searchByDirector, query);
    }
}
