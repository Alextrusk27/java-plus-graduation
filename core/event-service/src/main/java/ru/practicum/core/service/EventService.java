package ru.practicum.core.service;

import ru.practicum.core.dto.event.params.AdminSearchParams;
import ru.practicum.core.dto.event.params.EventParams;
import ru.practicum.core.dto.event.params.EventParamsSorted;
import ru.practicum.core.dto.event.params.PublicSearchParams;
import ru.practicum.core.dto.event.request.CreateEventDto;
import ru.practicum.core.dto.event.request.UpdateEventDto;
import ru.practicum.core.dto.event.response.*;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;

import java.util.List;

public interface EventService {

    EventDto create(CreateEventDto createRequest);

    EventDto update(UpdateEventDto updateRequest);

    EventDto adminUpdate(UpdateEventDto dto);

    EventDtoExtended get(Long id);

    EventDtoExtended get(EventParams params);

    List<EventDtoShortWithoutViews> get(EventParamsSorted params);

    List<EventDtoExtended> get(AdminSearchParams params);

    List<EventDtoShort> get(PublicSearchParams params);

    EventDtoInternal getInternalEvent(Long eventId);

    List<ParticipationRequestDto> getEventRequests(EventParams params);

    EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto);
}
