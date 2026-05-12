package ru.practicum.core.dto;


import ru.practicum.core.dto.event.response.EventDtoShortWithoutViews;

import java.util.List;

public record CompilationDto(
        Long id,
        Boolean pinned,
        String title,
        List<EventDtoShortWithoutViews> events
) {
}
