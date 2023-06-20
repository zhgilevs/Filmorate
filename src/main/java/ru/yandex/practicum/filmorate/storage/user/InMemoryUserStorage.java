package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int globalId = 0;

    @Override
    public List<User> getAll() {
        log.info("Запрошен список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
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

    @Override
    public User update(User updatedUser) {
        validateBirthday(updatedUser);
        int id = updatedUser.getId();
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с ID: '" + id + "' не найден");
        }
        if (StringUtils.isBlank(updatedUser.getName())) {
            updatedUser.setName(updatedUser.getLogin());
        }
        users.put(id, updatedUser);
        log.info("Пользователь с ID: '{}' обновлен", id);
        return updatedUser;
    }

    @Override
    public User getById(int id) {
        log.info("Запрошен пользователь с ID: '{}'", id);
        return users.get(id);
    }

    @Override
    public User addToFriends(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи с ID: '{}', '{}' добавили друг друга в друзья", userId, friendId);
        return user;
    }

    @Override
    public User removeFromFriends(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи с ID: '{}', '{}' удалили друг друга из друзей", userId, friendId);
        return user;
    }

    @Override
    public List<User> getFriendList(int id) {
        log.info("Запрошен список друзей пользователя с ID: '{}'", id);
        return users.get(id).getFriends().stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        final User userOne = users.get(id);
        final User userTwo = users.get(otherId);
        final Set<Integer> userOneFriends = userOne.getFriends();
        final Set<Integer> userTwoFriends = userTwo.getFriends();
        if (userOneFriends.isEmpty()) {
            log.info("У пользователя с ID: '{}' нет друзей", id);
            return Collections.emptyList();
        }
        if (userTwoFriends.isEmpty()) {
            log.info("У пользователя с ID: '{}' нет друзей", otherId);
            return Collections.emptyList();
        }
        List<User> commonFriends = userOneFriends.stream()
                .filter(userTwoFriends::contains)
                .map(users::get)
                .collect(Collectors.toList());
        if (commonFriends.isEmpty()) {
            log.info("У пользователей с ID: '{}' и '{}' нет общих друзей", id, otherId);
            return Collections.emptyList();
        } else {
            log.info("Запрошен список общих друзей у пользователей с ID: '{}' и '{}'", id, otherId);
            return commonFriends;
        }
    }

    @Override
    public boolean isExists(int id) {
        return users.containsKey(id);
    }

    private void validateBirthday(User user) {
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть передана в запросе");
        }
    }
}