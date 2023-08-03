package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "file:src/main/resources/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("Тесты DbUserStorage")
@Disabled
public class DbUserStorageTest {

    private static int k = 0;
    private final UserStorage storage;

    @Test
    @DisplayName("Создание пользователя")
    void createTest() {
        User user = storage.create(makeUser());
        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("Steve Wozniak", user.getName());
        assertEquals(k + "woz", user.getLogin());
        assertEquals("thesmartwozniak" + k + "@gmail.com", user.getEmail());
        assertEquals(LocalDate.of(1950, 8, 11), user.getBirthday());
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateTest() {
        storage.create(makeUser());
        User user = storage.getById(1);
        user.setName("Steve Wozniak Jr");
        User updatedUser = storage.update(user);
        assertEquals("Steve Wozniak Jr", updatedUser.getName());
    }

    @Test
    @DisplayName("Получение пользователя")
    void getByIdTest() {
        storage.create(makeUser());
        User user = storage.getById(1);
        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllTest() {
        storage.create(makeUser());
        storage.create(makeUser());
        List<User> users = storage.getAll();
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Добавление в друзья")
    void addToFriendsTest() {
        storage.create(makeUser());
        storage.create(makeUser());
        User user = storage.addToFriends(1, 2);
        assertEquals(1, user.getFriends().size());
    }

    @Test
    @DisplayName("Удаление из друзей")
    void removeFromFriends() {
        storage.create(makeUser());
        storage.create(makeUser());
        User user = storage.addToFriends(1, 2);
        assertEquals(1, user.getFriends().size());
        user = storage.removeFromFriends(1, 2);
        assertTrue(user.getFriends().isEmpty());
    }

    @Test
    @DisplayName("Получение списка друзей")
    void getFriendListTest() {
        storage.create(makeUser());
        storage.create(makeUser());
        storage.create(makeUser());
        storage.addToFriends(1, 2);
        storage.addToFriends(1, 3);
        List<User> friends = storage.getFriendList(1);
        assertEquals(2, friends.size());
    }

    @Test
    @DisplayName("Получение общих друзей")
    void getCommonFriends() {
        storage.create(makeUser());
        storage.create(makeUser());
        storage.create(makeUser());
        storage.addToFriends(1, 3);
        storage.addToFriends(2, 3);
        List<User> commons = storage.getCommonFriends(1, 2);
        assertEquals(1, commons.size());
    }

    @Test
    @DisplayName("Проверка наличия пользователя в базе")
    void isExistsTest() {
        storage.create(makeUser());
        boolean isExist = storage.isExists(1);
        assertTrue(isExist);
        isExist = storage.isExists(11);
        assertFalse(isExist);
    }

    private User makeUser() {
        k++;
        return User.builder()
                .name("Steve Wozniak")
                .email("thesmartwozniak" + k + "@gmail.com")
                .login(k + "woz")
                .birthday(LocalDate.of(1950, 8, 11))
                .build();
    }
}