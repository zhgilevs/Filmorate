package ru.yandex.practicum.filmorate.exception;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
    }
}
