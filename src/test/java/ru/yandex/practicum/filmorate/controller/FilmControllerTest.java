package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorageForTests;
import ru.yandex.practicum.filmorate.storage.event.EventStorageForTests;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование FilmController")
@Disabled
class FilmControllerTest {

    private static final String FILM_NAME = "nisi eiusmod";
    private static final String FILM_DESC = "adipisicing";
    private static final LocalDate FILM_RELEASE = LocalDate.of(1967, 3, 25);
    private static final int FILM_DURATION = 100;
    private static final String USER_NAME = "Nick Name";
    private static final String USER_EMAIL = "mail@mail.ru";
    private static final String USER_LOGIN = "dolore";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1946, 8, 20);
    private static final String WRONG_DESCRIPTION =
            "Пятеро друзей ( комик-группа «Шарлотка»),"
                    + "приезжают в город Новосибирск. Здесь они хотят разыскать господина Евгения Круглова,"
                    + "который задолжал им деньги, а именно 20 миллионов. о Круглов, который за время «своего отсутствия»"
                    + " стал кандидатом в президенты Колумбии.";
    private static final String VALIDATION_ERROR = "Ошибка валидации";
    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("Правильная валидация")
    @Test
    void correctFilm() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .directors(new HashSet<>())
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), VALIDATION_ERROR);
    }

    @DisplayName("Название не пустое")
    @Test
    void nameNotEmpty() {
        final Film film = Film.builder()
                .name("").description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .directors(new HashSet<>())
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Длина описания менее 200 символов")
    @Test
    void descriptionSizeLessThan200symbols() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(WRONG_DESCRIPTION).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .directors(new HashSet<>())
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Дата релиза не раньше 1895-12-28")
    @Test
    void releaseDateIsAfterStartPoint() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION)
                .releaseDate(LocalDate.of(1795, 12, 28))
                .mpa(new Mpa(1, null, null))
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        FilmController fc = new FilmController(service);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> fc.create(film));
        assertEquals("Дата релиза должна быть позже 1895-12-28", ex.getMessage(),
                "Исключение не выброшено");
    }

    @DisplayName("Дата релиза передана в запросе")
    @Test
    void releaseDateIsNotNull() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION)
                .releaseDate(null)
                .mpa(new Mpa(1, null, null))
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        FilmController fc = new FilmController(service);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> fc.create(film));
        assertEquals("Дата релиза должна быть передана в запросе", ex.getMessage(),
                "Исключение не выброшено");
    }

    @DisplayName("Продолжительность больше нуля")
    @Test
    void durationIsPositive() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(-5).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .directors(new HashSet<>())
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Фильм получен по id")
    @Test
    void getById() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        FilmController fc = new FilmController(service);
    }

    @DisplayName("Фильму поставлен лайк")
    @Test
    void setLike() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .build();
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        UserService userService = new UserService(userStorage, new EventService(new EventStorageForTests()));
        FilmController fc = new FilmController(filmService);
        UserController uc = new UserController(userService);
        fc.create(film);
        uc.create(user);
        fc.setLike(film.getId(), user.getId());
        assertEquals(1, film.getLikes().size());
    }

    @DisplayName("У фильма удален лайк")
    @Test
    void removeLike() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .build();
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        UserService userService = new UserService(userStorage, new EventService(new EventStorageForTests()));
        FilmController fc = new FilmController(filmService);
        UserController uc = new UserController(userService);
        fc.create(film);
        uc.create(user);
        fc.setLike(film.getId(), user.getId());
        fc.removeLike(film.getId(), user.getId());
        assertEquals(0, film.getLikes().size());
    }

    @DisplayName("Получен список популярных фильмов")
    @Test
    void getPopular() {
        final Film filmOne = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .build();
        final Film filmTwo = Film.builder()
                .name(FILM_NAME).description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .mpa(new Mpa(1, null, null))
                .build();
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage,
                new EventService(new EventStorageForTests()), new DirectorService(new DirectorStorageForTests()));
        UserService userService = new UserService(userStorage, new EventService(new EventStorageForTests()));
        FilmController fc = new FilmController(filmService);
        UserController uc = new UserController(userService);
        fc.create(filmOne);
        fc.create(filmTwo);
        uc.create(user);
        fc.setLike(filmOne.getId(), user.getId());
        fc.setLike(filmTwo.getId(), user.getId());
        assertEquals(2, fc.getPopularFilms(10).size());
    }
}