package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование валидации в FilmController")
class FilmControllerTest {

    private Validator validator;
    private static final String FILM_NAME = "nisi eiusmod";
    private static final String FILM_DESC = "adipisicing";
    private static final String WRONG_DESCRIPTION =
            "Пятеро друзей ( комик-группа «Шарлотка»),"
                    + "приезжают в город Новосибирск. Здесь они хотят разыскать господина Евгения Круглова,"
                    + "который задолжал им деньги, а именно 20 миллионов. о Круглов, который за время «своего отсутствия»"
                    + " стал кандидатом в президенты Колумбии.";
    private static final LocalDate FILM_RELEASE = LocalDate.of(1967, 3, 25);
    private static final int FILM_DURATION = 100;
    private static final String VALIDATION_ERROR = "Ошибка валидации";

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
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), VALIDATION_ERROR);
    }

    @DisplayName("Название не пустое")
    @Test
    void nameNotEmpty() {
        final Film film = Film.builder()
                .name("").description(FILM_DESC).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Длина описания менее 200 символов")
    @Test
    void descriptionSizeLessThan200symbols() {
        final Film film = Film.builder()
                .name(FILM_NAME).description(WRONG_DESCRIPTION).duration(FILM_DURATION).releaseDate(FILM_RELEASE)
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
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage);
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
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService service = new FilmService(filmStorage, userStorage);
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
                .build();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }
}