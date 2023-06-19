package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getAll();

    User create(User user);

    User update(User user);

    User getById(int id);

    User addToFriends(int userId, int friendId);

    User removeFromFriends(int userId, int friendId);

    List<User> getFriendList(int id);

    List<User> getCommonFriends(int id, int otherId);

    boolean isExists(int id);
}