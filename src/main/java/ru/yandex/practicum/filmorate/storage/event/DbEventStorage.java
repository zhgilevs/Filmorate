package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Component("DbEventStorage")
@Primary
@RequiredArgsConstructor
public class DbEventStorage implements EventStorage {
    private final JdbcOperations jdbcTemplate;

    @Override
    public void addEvent(Event event) {
        event.setTimestamp(Instant.now().toEpochMilli());

        String sqlQuery = "INSERT INTO feeds (userId, entityId, timestamp, eventType, operation) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            stmt.setInt(1, event.getUserId());
            stmt.setInt(2, event.getEntityId());
            stmt.setLong(3, event.getTimestamp());
            stmt.setString(4, event.getEventType());
            stmt.setString(5, event.getOperation());
            return stmt;
        }, keyHolder);
    }

    @Override
    public List<Event> getUserFeeds(int userId) {
        String sqlQuery = "SELECT * FROM feeds WHERE userId=? ORDER BY id";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeEvent(rs), userId);
    }

    private Event makeEvent(ResultSet rs) {
        try {
            return Event.builder()
                    .eventId(rs.getInt("ID"))
                    .userId(rs.getInt("userId"))
                    .entityId(rs.getInt("entityId"))
                    .timestamp(rs.getLong("timestamp"))
                    .eventType(rs.getString("eventType"))
                    .operation(rs.getString("operation"))
                    .build();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения пользователя");
        }
    }
}
