package ru.practicum.core.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMAT;

public record ParticipationRequestDto(
        Long id,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime created,

        Long event,
        Long requester,
        String status
) {
}
