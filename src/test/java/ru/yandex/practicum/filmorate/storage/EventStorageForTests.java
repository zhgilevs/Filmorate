package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.ArrayList;
import java.util.List;

public class EventStorageForTests implements EventStorage {
    @Override
    public void addEvent(Event event) {

    }

    @Override
    public List<Event> getUserFeeds(int userId) {
        return new ArrayList<>();
    }
}
