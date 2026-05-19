package ru.practicum.core.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.core.dto.user.response.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMAT;

public record CommentDto(
        Long id,
        String text,
        UserShortDto author,

        @JsonFormat(pattern = DATE_TIME_FORMAT)
        LocalDateTime createdOn,
        Long eventId
) {
}
