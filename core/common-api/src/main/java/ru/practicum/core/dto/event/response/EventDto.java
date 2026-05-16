package ru.practicum.core.dto.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.core.dto.TEMPORARY.Location;
import ru.practicum.core.dto.TEMPORARY.State;
import ru.practicum.core.dto.category.response.CategoryDto;
import ru.practicum.core.dto.user.response.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMAT;

public record EventDto(
        Long id,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime createdOn,

        String title,
        String annotation,
        String description,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime eventDate,

        State state,
        Boolean paid,
        Integer participantLimit,
        Boolean requestModeration,
        CategoryDto category,
        UserShortDto initiator,
        Location location,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime publishedOn
) {
}
