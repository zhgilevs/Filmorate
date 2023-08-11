package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class User {

    @JsonIgnore
    private final Set<Integer> friends = new HashSet<>();
    private int id;
    private String name;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "\\S+")
    private String login;
    @PastOrPresent
    private LocalDate birthday;

    public void setFriends(Set<Integer> set) {
        friends.addAll(set);
    }

}
