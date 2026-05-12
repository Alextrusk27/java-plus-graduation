package ru.practicum.core.dto.event.request;

import ru.practicum.core.dto.TEMPORARY.StateAction;

import java.time.LocalDateTime;

public record UpdateEventDto(
        String annotation,
        Long category,
        String description,
        LocalDateTime eventDate,
        LocationBody location,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        StateAction stateAction,
        String title,
        Long initiatorId,
        Long eventId
) {
    public static UpdateEventDto of(UpdateEventBody body, Long initiatorId, Long eventId) {
        return new UpdateEventDto(
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.stateAction(),
                body.title(),
                initiatorId,
                eventId
        );
    }

    public static UpdateEventDto of(AdminUpdateEventBody body, Long eventId) {
        return new UpdateEventDto(
                body.annotation(),
                body.category(),
                body.description(),
                body.eventDate(),
                body.location(),
                body.paid(),
                body.participantLimit(),
                body.requestModeration(),
                body.stateAction(),
                body.title(),
                null,
                eventId
        );
    }

    public boolean hasStateAction() {
        return this.stateAction != null;
    }
}