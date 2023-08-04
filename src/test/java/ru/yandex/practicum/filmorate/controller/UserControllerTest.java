package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.event.EventStorageForTests;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование UserController")
@Disabled
class UserControllerTest {

    private static final String USER_NAME = "Nick Name";
    private static final String USER_EMAIL = "mail@mail.ru";
    private static final String USER_LOGIN = "dolore";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1946, 8, 20);
    private static final String VALIDATION_ERROR = "Ошибка валидации";
    private Validator validator;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("Правильная валидация")
    @Test
    void correctUser() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), VALIDATION_ERROR);
    }

    @DisplayName("Логин не пустой")
    @Test
    void emptyLogin() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(null).birthday(USER_BIRTHDAY)
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Логин не содержит пробелы")
    @Test
    void loginContainsSpaces() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login("the best user in the world").birthday(USER_BIRTHDAY)
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Логин равен имени, если имя не задано")
    @Test
    void nameShouldBeEqualsLoginIfNameIsNull() {
        final User user = User.builder()
                .name(null).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        User createdUser = uc.create(user);
        assertEquals(USER_LOGIN, createdUser.getName(), VALIDATION_ERROR);
    }

    @DisplayName("Логин равен имени, если имя пустое")
    @Test
    void nameShouldBeEqualsLoginIfNameIsEmpty() {
        final User user = User.builder()
                .name("").email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        User createdUser = uc.create(user);
        assertEquals(USER_LOGIN, createdUser.getName(), VALIDATION_ERROR);
    }

    @DisplayName("Дата рождения не является датой в будущем")
    @Test
    void birthdayNotInTheFuture() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN)
                .birthday(LocalDate.of(2123, 5, 16))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Дата рождения передана в запросе")
    @Test
    void birthdayNotNull() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN)
                .birthday(null)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        ValidationException ex = assertThrows(ValidationException.class,
                () -> uc.create(user));
        assertEquals("Дата рождения должна быть передана в запросе", ex.getMessage(),
                "Исключение не выброшено");
    }

    @DisplayName("Email не пустой")
    @Test
    void emailNotEmpty() {
        final User user = User.builder()
                .name(USER_NAME).email("").login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Email имеет верный формат")
    @Test
    void emailHasCorrectFormat() {
        final User user = User.builder()
                .name(USER_NAME).email("mail&mail.ru").login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size(), VALIDATION_ERROR);
    }

    @DisplayName("Пользователь получен по id")
    @Test
    void getById() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        uc.create(user);
        assertEquals(1, uc.getById(user.getId()).getId());
    }

    @DisplayName("Пользователи добавляются в друзья")
    @Test
    void addToFriends() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        final User friend = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        uc.create(user);
        uc.create(friend);
        uc.addToFriends(user.getId(), friend.getId());
        assertEquals(1, user.getFriends().size());
        assertEquals(1, friend.getFriends().size());
    }

    @DisplayName("Пользователи удаляются из друзей")
    @Test
    void removeFromFriends() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        final User friend = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        uc.create(user);
        uc.create(friend);
        uc.addToFriends(user.getId(), friend.getId());
        uc.removeFromFriends(user.getId(), friend.getId());
        assertEquals(0, user.getFriends().size());
        assertEquals(0, friend.getFriends().size());
    }

    @DisplayName("Список друзей пользователя получен")
    @Test
    void getFriendList() {
        final User user = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        final User friend = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        uc.create(user);
        uc.create(friend);
        uc.addToFriends(user.getId(), friend.getId());
        assertEquals(1, uc.getFriendList(user.getId()).size());
        assertEquals(1, uc.getFriendList(friend.getId()).size());
    }

    @DisplayName("Получен список общих друзей")
    @Test
    void getCommonFriends() {
        final User userOne = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        final User userTwo = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        final User commonFriend = User.builder()
                .name(USER_NAME).email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventStorageForTests()));
        UserController uc = new UserController(service, new EventService(new EventStorageForTests()));
        uc.create(userOne);
        uc.create(userTwo);
        uc.create(commonFriend);
        uc.addToFriends(userOne.getId(), commonFriend.getId());
        uc.addToFriends(userTwo.getId(), commonFriend.getId());
        assertEquals(1, uc.getCommonFriends(userOne.getId(), userTwo.getId()).size());
    }
}