package ru.practicum.core.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;

import java.util.List;
import java.util.Map;

public interface RequestService {
    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    Long countByEventAndStatus(Long eventId, String status);

    Map<Long, Long> countByEventsAndStatus(List<Long> eventIds, String status);

    List<ParticipationRequestDto> getByEventId(Long eventId, Pageable pageable);

    EventRequestStatusUpdateResult updateRequestsStatus(UpdateRequestStatusDto dto, Integer participantLimit);
}
