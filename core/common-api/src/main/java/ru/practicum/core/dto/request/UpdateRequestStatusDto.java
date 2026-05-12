package ru.practicum.core.dto.request;

import java.util.List;

public record UpdateRequestStatusDto(
        List<Long> requestIds,
        String status,
        Long userId,
        Long eventId
) {
    public static UpdateRequestStatusDto of(
            UpdateRequestStatusBody body,
            Long userId,
            Long eventId
    ) {
        return new UpdateRequestStatusDto(
                body.requestIds(),
                body.status(),
                userId,
                eventId
        );
    }
}
