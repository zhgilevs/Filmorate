package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final Logger log;
    private final EventStorage eventStorage;

    @Autowired
    public EventService(@Qualifier("DbEventStorage") EventStorage eventStorage) {
        this.log = LoggerFactory.getLogger("EventService");
        this.eventStorage = eventStorage;
    }

    public void addEvent(Event event) {

    }

    public List<Event> getFeedByUserId(int id) {
        return null;
    }
}
