package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    private static final String ERROR = "Ошибка: ";
    private static final String ERROR_400 = "Ошибка 400: ";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleException(final Exception e) {
        log.warn(ERROR + e.getMessage());
        return new ErrorResponse(ERROR_400, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.warn(ERROR + e.getMessage());
        return new ErrorResponse("Ошибка 404: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidationException(final ValidationException e) {
        log.warn(ERROR + e.getMessage());
        return new ErrorResponse(ERROR_400, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn(ERROR + e.getMessage());
        return new ErrorResponse(ERROR_400, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDatabaseException(final DatabaseException e) {
        log.warn(ERROR, e);
        return new ErrorResponse(ERROR, e.getMessage());
    }
}
