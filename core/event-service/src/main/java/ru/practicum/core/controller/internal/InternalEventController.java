package ru.practicum.core.controller.internal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.service.EventService;
import ru.practicum.core.utils.ApiPaths;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Internal.EVENTS)
@Validated
@Slf4j
public class InternalEventController {
    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventDtoInternal get(@PathVariable @NotNull @Positive Long eventId) {

        log.debug("Internal request: get event by id {}", eventId);
        var result = eventService.getInternalEvent(eventId);
        log.debug("Internal response: event {}", result);
        return result;
    }

}
