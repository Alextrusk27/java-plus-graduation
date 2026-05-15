package ru.practicum.core.dto.compilation.response;

import ru.practicum.core.dto.event.response.EventDtoShortWithoutViews;

import java.util.List;

public record CompilationDto(
        Long id,
        Boolean pinned,
        String title,
        List<EventDtoShortWithoutViews> events
) {
}
