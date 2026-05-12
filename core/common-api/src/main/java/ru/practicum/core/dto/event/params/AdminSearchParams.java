package ru.practicum.core.dto.event.params;

import org.springframework.data.domain.Pageable;
import ru.practicum.core.utils.PageableFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMATTER;
import static ru.practicum.core.utils.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record AdminSearchParams(
        List<Long> users,
        List<String> states,
        List<Long> categories,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        Pageable pageable
) {
    public static AdminSearchParams of(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd,
            Integer from,
            Integer size) {

        Pageable pageable = PageableFactory.offset(from, size, EVENTS_DEFAULT_SORT);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null && !rangeStart.isBlank()) {
            start = LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER);
        }

        if (rangeEnd != null && !rangeEnd.isBlank()) {
            end = LocalDateTime.parse(rangeEnd, DATE_TIME_FORMATTER);
        }

        if (states == null) {
            states = List.of();
        }

        return new AdminSearchParams(
                users,
                states,
                categories,
                start,
                end,
                pageable
        );
    }
}
