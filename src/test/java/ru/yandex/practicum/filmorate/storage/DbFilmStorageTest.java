package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты DbFilmStorage")
class DbFilmStorageTest {

    private static final String FILM_NAME = "The Godfather";
    private static final String FILM_DESCRIPTION = "Don Vito Corleone, head of a mafia family, decides to hand over his empire to his youngest son Michael. However, his decision unintentionally puts the lives of his loved ones in grave danger";
    private static final int FILM_DURATION = 175;
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(1972, 3, 14);
    private static final Mpa MPA = new Mpa(4, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого");
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    @DisplayName("Создание фильма")
    void createTest() {
        Film film = filmStorage.create(makeFilm());
        assertNotNull(film);
        assertEquals(1, film.getId());
        assertEquals(FILM_NAME, film.getName());
        assertEquals(FILM_DESCRIPTION, film.getDescription());
        assertEquals(FILM_DURATION, film.getDuration());
        assertEquals(FILM_RELEASE_DATE, film.getReleaseDate());
        assertEquals(MPA.getId(), film.getMpa().getId());
    }

    @Test
    @DisplayName("Обновление фильма")
    void updateTest() {
        filmStorage.create(makeFilm());
        Film film = filmStorage.getById(1);
        film.setDescription("Updated description");
        Film updatedFilm = filmStorage.update(film);
        assertEquals("Updated description", updatedFilm.getDescription());
    }

    @Test
    @DisplayName("Получение фильма")
    void getByIdTest() {
        filmStorage.create(makeFilm());
        Film film = filmStorage.getById(1);
        assertNotNull(film);
        assertEquals(1, film.getId());
    }

    @Test
    @DisplayName("Получение всех фильмов")
    void getAllTest() {
        filmStorage.create(makeFilm());
        filmStorage.create(makeFilm());
        List<Film> films = filmStorage.getAll();
        assertEquals(2, films.size());
    }

    @Test
    @DisplayName("Добавление лайка")
    void addLikeTest() {
        userStorage.create(makeUser());
        filmStorage.create(makeFilm());
        filmStorage.addLike(1, 1);
        assertEquals(1, filmStorage.getById(1).getLikes().size());
    }

    @Test
    @DisplayName("Удаление лайка")
    void removeLikeTest() {
        userStorage.create(makeUser());
        filmStorage.create(makeFilm());
        filmStorage.addLike(1, 1);
        assertEquals(1, filmStorage.getById(1).getLikes().size());
        filmStorage.removeLike(1, 1);
        assertTrue(filmStorage.getById(1).getLikes().isEmpty());
    }

    @Test
    @DisplayName("Получение списка популярных фильмов")
    void getPopularTest() {
        filmStorage.create(makeFilm());
        filmStorage.create(makeFilm());
        filmStorage.create(makeFilm());
        filmStorage.create(makeFilm());
        List<Film> populars = filmStorage.getPopular(3);
        assertEquals(3, populars.size());
    }

    @Test
    @DisplayName("Проверка наличия фильма в базе")
    void isExistsTest() {
        filmStorage.create(makeFilm());
        boolean isExist = filmStorage.isExists(1);
        assertTrue(isExist);
        isExist = filmStorage.isExists(11);
        assertFalse(isExist);
    }


    private Film makeFilm() {
        return Film.builder()
                .name(FILM_NAME)
                .description(FILM_DESCRIPTION)
                .duration(FILM_DURATION)
                .releaseDate(FILM_RELEASE_DATE)
                .mpa(MPA)
                .directors(new HashSet<>())
                .build();
    }

    private User makeUser() {
        return User.builder()
                .name("Steve Wozniak")
                .email("thesmartwozniak@gmail.com")
                .login("woz")
                .birthday(LocalDate.of(1950, 8, 11))
                .build();
    }
}