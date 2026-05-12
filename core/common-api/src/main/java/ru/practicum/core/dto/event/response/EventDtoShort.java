package ru.practicum.core.dto.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.core.dto.category.response.CategoryDto;
import ru.practicum.core.dto.user.response.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMAT;

public record EventDtoShort(
        String annotation,
        CategoryDto category,
        Long confirmedRequests,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime eventDate,

        Long id,
        UserShortDto initiator,
        Boolean paid,
        String title,
        Long views
) {
}
