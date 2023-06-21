package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "Пользователь с ID: '%s' не найден";
    private final UserStorage storage;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
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
        return storage.addToFriends(userId, friendId);
    }

    public User removeFromFriends(int userId, int friendId) {
        if (!storage.isExists(userId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, userId));
        }
        if (!storage.isExists(friendId)) {
            throw new NotFoundException(String.format(USER_NOT_FOUND, friendId));
        }
        return storage.removeFromFriends(userId, friendId);
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
        return storage.create(user);
    }

    public User update(User user) {
        return storage.update(user);
    }
}