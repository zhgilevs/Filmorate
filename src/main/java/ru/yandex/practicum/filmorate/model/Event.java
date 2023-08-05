package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Event {
    private Integer eventId;
    @NotNull
    private Integer userId;
    @NotNull
    private Integer entityId;
    private Long timestamp;
    @NotNull
    private String eventType;
    @NotNull
    private String operation;
}
