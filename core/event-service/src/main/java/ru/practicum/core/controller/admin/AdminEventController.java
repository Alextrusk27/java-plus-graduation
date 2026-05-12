package ru.practicum.core.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.event.params.AdminSearchParams;
import ru.practicum.core.dto.event.request.AdminUpdateEventBody;
import ru.practicum.core.dto.event.request.UpdateEventDto;
import ru.practicum.core.dto.event.response.EventDto;
import ru.practicum.core.dto.event.response.EventDtoExtended;
import ru.practicum.core.service.EventService;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.EVENTS)
@Validated
@Slf4j
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<EventDtoExtended> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("ADMIN: Get EVENTS with params: from={}, size={}", from, size);
        AdminSearchParams params = AdminSearchParams.of(users, states, categories, rangeStart, rangeEnd, from, size);
        List<EventDtoExtended> result = service.get(params);
        log.info("ADMIN: Found {} EVENTS", result.size());
        return result;
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(
            @PathVariable @Positive Long eventId,
            @RequestBody @Valid AdminUpdateEventBody body) {

        log.info("ADMIN: Update event {}", eventId);
        UpdateEventDto dto = UpdateEventDto.of(body, eventId);
        EventDto result = service.adminUpdate(dto);
        log.info("ADMIN: Updated event {}", result.id());
        return result;
    }
}
