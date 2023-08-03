package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты DbMpaStorage")
@Disabled
public class DbMpaStorageTest {

    private final MpaStorage storage;

    @Test
    @DisplayName("Получение ретинга")
    void getByIdTest() {
        Mpa pg = storage.getById(3);
        assertNotNull(pg);
        assertEquals("PG-13", pg.getName());
    }

    @Test
    @DisplayName("Получение всех рейтингов")
    void getAllTest() {
        List<Mpa> mpas = storage.getAll();
        assertEquals(5, mpas.size());

    }
}
