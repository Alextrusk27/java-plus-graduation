package ru.practicum.core.dto.event.request;

import java.time.LocalDateTime;

public record CreateEventDto(
        Long initiatorId,
        String annotation,
        Long category,
        String description,
        LocalDateTime eventDate,
        LocationBody location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        String title
) {
    public static CreateEventDto of(CreateEventBody body, Long initiatorId) {
        return new CreateEventDto(
                initiatorId,
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.title()
        );
    }
}
