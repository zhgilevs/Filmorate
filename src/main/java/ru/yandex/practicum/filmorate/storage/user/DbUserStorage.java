package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component("DbUserStorage")
@Primary
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final JdbcOperations jdbcTemplate;

    @Override
    public List<User> getAll() {
        String sqlQuery = "SELECT * FROM USERS";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO USERS (NAME, LOGIN, EMAIL, BIRTHDAY) VALUES (?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);
        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();
        String sqlQuery = "UPDATE USERS SET NAME=?, LOGIN=?, EMAIL=?, BIRTHDAY=? WHERE ID=?";
        jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                id);
        return getById(id);
    }

    @Override
    public User getById(int id) {
        String sqlQuery = "SELECT * FROM USERS WHERE ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            User user = User.builder()
                    .id(userRows.getInt("ID"))
                    .name(userRows.getString("NAME"))
                    .login(userRows.getString("LOGIN"))
                    .email(userRows.getString("EMAIL"))
                    .birthday(Objects.requireNonNull(userRows.getDate("BIRTHDAY")).toLocalDate())
                    .build();
            user.setFriends(new HashSet<>(getFriendsIdByUserId(id)));
            return user;
        } else {
            throw new NotFoundException("Пользователь с ID: '" + id + "' не найден");
        }
    }

    @Override
    public User addToFriends(int userId, int friendId) {
        boolean isFriendExists = false;
        String sqlQuery = "SELECT * FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, friendId, userId);
        if (userRows.next()) {
            isFriendExists = true;
        }
        if (!isFriendExists) {
            sqlQuery = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID) values (?, ?)";
            jdbcTemplate.update(sqlQuery, userId, friendId);
        }
        return getById(userId);
    }

    @Override
    public User removeFromFriends(int userId, int friendId) {
        String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID=? AND FRIEND_ID=?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        return getById(userId);
    }

    @Override
    public List<User> getFriendList(int id) {
        String sqlQuery = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getFriend(rs), id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        List<User> commonFriends = new ArrayList<>();
        String sqlQuery = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=? " +
                "AND FRIEND_ID IN (SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID=?)";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id, otherId);
        if (userRows.next()) {
            commonFriends.add(getById(userRows.getInt("FRIEND_ID")));
        }
        return commonFriends;
    }

    @Override
    public boolean isExists(int id) {
        String sqlQuery = "SELECT ID FROM USERS WHERE ID=?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (userRows.next()) {
            int idFromUsers = userRows.getInt("ID");
            return idFromUsers == id;
        }
        return false;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) {
        try {
            return User.builder()
                    .id(resultSet.getInt("ID"))
                    .name(resultSet.getString("NAME"))
                    .login(resultSet.getString("LOGIN"))
                    .email(resultSet.getString("EMAIL"))
                    .birthday(resultSet.getDate("BIRTHDAY").toLocalDate())
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения пользователя");
        }
    }

    private User getFriend(ResultSet rs) {
        try {
            return getById(rs.getInt("FRIEND_ID"));
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения друга из база");
        }
    }

    private List<Integer> getFriendsIdByUserId(int id) {
        String sqlQuery = "SELECT FRIEND_ID from FRIENDS where USER_ID=?";
        return (jdbcTemplate.query(sqlQuery, (rs, rowNum) -> getFriendId(rs), id));
    }

    private Integer getFriendId(ResultSet rs) {
        try {
            return rs.getInt("FRIEND_ID");
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения друга из база");
        }
    }

    public User deleteUserById(int id) {
        User user = getById(id);
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, user.getId());
        return user;
    }
}