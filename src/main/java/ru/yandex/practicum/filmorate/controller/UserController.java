package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;

    @PostMapping
    User create(@RequestBody @Valid User user) {
        return service.create(user);
    }

    @PutMapping
    User update(@RequestBody @Valid User updatedUser) {
        return service.update(updatedUser);
    }

    @GetMapping
    List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    User getById(@PathVariable(value = "id") int id) {
        return service.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    User addToFriends(@PathVariable int id,
                      @PathVariable int friendId) {
        return service.addToFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    User removeFromFriends(@PathVariable int id,
                           @PathVariable int friendId) {
        return service.removeFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    List<User> getFriendList(@PathVariable int id) {
        return service.getFriendList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    List<User> getCommonFriends(@PathVariable int id,
                                @PathVariable int otherId) {
        return service.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendedFilmForUser(@PathVariable int id){
        return service.getRecommendedFilmForUser(id);
    }


}