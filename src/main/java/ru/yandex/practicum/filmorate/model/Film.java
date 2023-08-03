package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import ru.yandex.practicum.filmorate.model.director.Director;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Film {

    private int id;
    @JsonIgnore
    private final Set<Integer> likes = new HashSet<>();
    private final Set<Genre> genres = new HashSet<>();
    @NotBlank
    @Size(min = 1, max = 200, message = "Описание должно быть не более 200 символов и не менее 1 символа")
    private String description;
    @NotNull
    @Positive
    private int duration;
    @NotBlank
    private String name;
    private LocalDate releaseDate;
    private Mpa mpa;
    /**
     * Режиссеры фильма.
     */
    private Set<Director> directors;

    public void setLikes(Set<Integer> set) {
        likes.addAll(set);
    }

    public void setGenres(Set<Genre> set) {
        genres.addAll(set);
    }
}