package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;
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
        if (user.getName() == null || user.getName().isBlank()) {
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
        int id = updatedUser.getId();
        if (users.containsKey(id)) {
            users.put(id, updatedUser);
            log.info("Пользователь с ID: '{}' обновлен", id);
            return updatedUser;
        } else {
            log.warn("Пользователя нет в базе");
            throw new ValidationException("Пользователь с ID: '" + id + "' отсутствует в базе!");
        }
    }

    @GetMapping
    List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}