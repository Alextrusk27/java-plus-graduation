package ru.practicum.core.dto.event.params;

import org.springframework.data.domain.Pageable;
import ru.practicum.core.utils.PageableFactory;

import static ru.practicum.core.utils.constants.AppConstants.EVENTS_DEFAULT_SORT;

public record EventParamsSorted(
        Long userId,
        Pageable pageable
) {
    public static EventParamsSorted of(Long userId, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, EVENTS_DEFAULT_SORT);

        return new EventParamsSorted(
                userId,
                pageable
        );
    }
}
