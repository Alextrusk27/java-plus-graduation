package ru.practicum.core.dto.event.response;

import ru.practicum.core.dto.TEMPORARY.State;

public record EventDtoInternal(
        Long id,
        Long initiatorId,
        State state,
        Integer participantLimit,
        Boolean requestModeration
) {
}
