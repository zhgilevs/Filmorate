package ru.yandex.practicum.filmorate.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "Пользователь с ID: '%s' не найден";
    private final UserStorage storage;

    private final EventService eventService;

    @Autowired
    public UserService(
            @Qualifier("DbUserStorage") UserStorage storage, EventService eventService) {
        this.storage = storage;
        this.eventService = eventService;
    }

    public User getById(int id) {
        if (storage.isExists(id)) {
            return storage.getById(id);
        } else {
            throw new NotFoundException(String.format(USER_NOT_FOUND, id));
        }
    }

    public User addToFriends(int userId, int friendId) {
        if (!storage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }
        if (!storage.isExists(friendId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, friendId));
        }

        User userInReturningCondition = storage.addToFriends(userId, friendId);

        eventService.addEvent("FRIEND", "ADD", userId, friendId);

        return userInReturningCondition;
    }

    public User removeFromFriends(int userId, int friendId) {
        if (!storage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }
        if (!storage.isExists(friendId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, friendId));
        }

        User userInReturningCondition = storage.removeFromFriends(userId, friendId);

        eventService.addEvent("FRIEND", "Remove", userId, friendId);

        return userInReturningCondition;
    }

    public List<User> getFriendList(int id) {
        if (!storage.isExists(id)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, id));
        } else {
            return storage.getFriendList(id);
        }
    }

    public List<User> getCommonFriends(int id, int otherId) {
        if (!storage.isExists(id)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, id));
        }
        if (!storage.isExists(otherId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, otherId));
        }
        return storage.getCommonFriends(id, otherId);
    }

    public List<User> getAll() {
        return storage.getAll();
    }

    public User create(User user) {
        validate(user);
        return storage.create(user);
    }

    public User update(User user) {
        validate(user);
        return storage.update(user);
    }

    private void validate(User user) {
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть передана в запросе");
        }
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
    }

    public User deleteUserById(int id) {
        if (storage.isExists(id)) {
            return storage.deleteUserById(id);
        } else {
            throw new NotFoundException(String.format(USER_NOT_FOUND, id));
        }
    }
}