package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Film {

    @JsonIgnore
    private final Set<Integer> likes = new HashSet<>();
    private final Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId, (id1, id2) -> id1 - id2));
    private int id;
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
    private HashSet<Director> directors;

    public void setLikes(Set<Integer> set) {
        likes.addAll(set);
    }

    public void setGenres(Set<Genre> set) {
        genres.addAll(set);
    }

    public Film() {
    }
}