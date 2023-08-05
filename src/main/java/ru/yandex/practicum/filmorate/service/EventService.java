package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final Logger log;
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    @Autowired
    public EventService(@Qualifier("DbEventStorage") EventStorage eventStorage,
                        @Qualifier("DbUserStorage") UserStorage userStorage) {
        this.log = LoggerFactory.getLogger("EventService");
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public void addEvent(Event event) {
        //проверка на присутствие пользователя выполняется перед каждым вызовом метода
        log.debug("Добавление события пользователя {} {} {} {}.",
                event.getUserId(), event.getOperation(), event.getEventType(), event.getEntityId());
        eventStorage.addEvent(event);
    }

    public List<Event> getUserFeeds(int id) {
        log.debug("Получен запрос ленты новостей пользователя {}.", id);
        checkIdForPresentsInUserDb(id);
        return eventStorage.getUserFeeds(id);
    }

    private void checkIdForPresentsInUserDb(int id) {
        userStorage.getById(id); //этот метод отправляет ошибку, если нет пользователя
    }
}
