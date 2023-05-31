package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int globalId = 0;

    @PostMapping
    User create(@RequestBody @Valid User user) {
        validateBirthday(user);
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
        int id = ++globalId;
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь: '{}' создан", user);
        return user;
    }

    @PutMapping
    User update(@RequestBody @Valid User updatedUser) {
        validateBirthday(updatedUser);
        int id = updatedUser.getId();
        if (!users.containsKey(id)) {
            log.warn("Пользователя нет в базе");
            throw new ValidationException("Пользователь с ID: '" + id + "' отсутствует в базе!");
        }
        if (StringUtils.isBlank(updatedUser.getName())) {
            updatedUser.setName(updatedUser.getLogin());
        }
        users.put(id, updatedUser);
        log.info("Пользователь с ID: '{}' обновлен", id);
        return updatedUser;
    }

    @GetMapping
    List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    private void validateBirthday(User user) {
        if (user.getBirthday() == null) {
            log.warn("Дата рождения не передана в запросе");
            throw new ValidationException("Дата рождения должна быть передана в запросе");
        }
    }
}