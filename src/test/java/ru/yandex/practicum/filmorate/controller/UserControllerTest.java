package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тестирование валидации в UserController")
class UserControllerTest {

    private Validator validator;
    private static final String USER_NAME = "Nick Name";
    private static final String USER_EMAIL = "mail@mail.ru";
    private static final String USER_LOGIN = "dolore";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1946, 8, 20);
    private static final String VALIDATION_ERROR = "Ошибка валидации";

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
        UserController uc = new UserController();
        User createdUser = uc.create(user);
        assertEquals(USER_LOGIN, createdUser.getName(), VALIDATION_ERROR);
    }

    @DisplayName("Логин равен имени, если имя пустое")
    @Test
    void nameShouldBeEqualsLoginIfNameIsEmpty() {
        final User user = User.builder()
                .name("").email(USER_EMAIL).login(USER_LOGIN).birthday(USER_BIRTHDAY)
                .build();
        UserController uc = new UserController();
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
}