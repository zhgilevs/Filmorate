package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты DbGenreStorage")
public class DbGenreStorageTest {

    private final GenreStorage storage;

    @Test
    @DisplayName("Получение жанра")
    void getByIdTest() {
        Genre comedy = storage.getById(1);
        assertNotNull(comedy);
        assertEquals("Комедия", comedy.getName());
    }

    @Test
    @DisplayName("Получение всех жанров")
    void getAllTest() {
        List<Genre> genres = storage.getAll();
        assertEquals(6, genres.size());
    }
}
