package ru.practicum.core.exception;

import org.springframework.http.HttpStatus;
import ru.practicum.core.utils.constants.AppConstants;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        String timestamp,
        HttpStatus status,
        String message,
        List<String> errors
) {

    public static ApiError of(HttpStatus status, String message, List<String> errors) {
        return new ApiError(
                LocalDateTime.now().format(AppConstants.DATE_TIME_FORMATTER),
                status,
                message,
                errors
        );
    }
}
