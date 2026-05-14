package ru.practicum.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.dto.TEMPORARY.State;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.exception.AccessException;
import ru.practicum.core.exception.ConflictException;
import ru.practicum.core.feign.client.EventFeignClient;
import ru.practicum.core.feign.client.UserFeignClient;
import ru.practicum.core.model.ParticipationRequest;
import ru.practicum.core.model.RequestStatus;
import ru.practicum.core.model.mapper.RequestMapper;
import ru.practicum.core.repository.RequestRepository;
import ru.practicum.core.utils.BaseService;
import ru.practicum.core.utils.EntityName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.practicum.core.model.RequestStatus.CANCELED;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl extends BaseService implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    private final UserFeignClient userFeignClient;
    private final EventFeignClient eventFeignClient;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {

        UserDto user = findUserOrThrow(userId);
        EventDtoInternal event = findEventOrThrow(eventId);

        if (event.state() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot participate in event %d: event is not published (current state: %s)"
                            .formatted(eventId, event.state())
            );
        }

        if (requestRepository.existsByEventIdAndRequesterId(event.id(), user.id())) {
            throw new ConflictException(
                    "User %d already has a participation request for event %d"
                            .formatted(user.id(), event.id())
            );
        }

        if (event.initiatorId().equals(user.id())) {
            throw new ConflictException(
                    "User %d cannot participate in their own event %d"
                            .formatted(user.id(), event.id())
            );
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.participantLimit() > 0
                && !event.requestModeration()
                && event.participantLimit() <= confirmedRequests) {
            throw new ConflictException(
                    "Event %d has reached participant limit (%d/%d)"
                            .formatted(eventId, confirmedRequests, event.participantLimit())
            );
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEventId(event.id());
        request.setRequesterId(user.id());

        RequestStatus status = (!event.requestModeration() || event.participantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        request.setStatus(status);

        ParticipationRequest saved = requestRepository.save(request);

        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> get(Long userId) {
        UserDto user = findUserOrThrow(userId);

        return requestRepository.findAllByRequesterId(user.id()).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        UserDto user = findUserOrThrow(userId);

        ParticipationRequest request = findRequestOrThrow(requestId);

        if (!request.getRequesterId().equals(user.id())) {
            throw new AccessException(
                    "User %d attempted to cancel request %d, but is not the requester"
                            .formatted(userId, requestId)
            );
        }

        request.setStatus(CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);

        return requestMapper.toDto(savedRequest);
    }

    @Override
    public Long countByEventAndStatus(Long eventId, String status) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.valueOf(status));
    }

    @Override
    public Map<Long, Long> countByEventsAndStatus(List<Long> eventIds, String status) {
        return requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.valueOf(status));
    }

    @Override
    public List<ParticipationRequestDto> getByEventId(Long eventId, Pageable pageable) {
        return requestRepository.findAllByEventId(eventId, pageable).getContent().stream()
                .map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(UpdateRequestStatusDto dto, Integer participantLimit) {

        Long confirmedCount = requestRepository.countByEventIdAndStatus(dto.eventId(), RequestStatus.CONFIRMED);

        List<ParticipationRequest> requests = requestRepository.findAllById(dto.requestIds());

        List<ParticipationRequest> toConfirm = new ArrayList<>();
        List<ParticipationRequest> toReject = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(
                        "Request %d is not in PENDING state (current: %s). Only PENDING requests can be updated"
                                .formatted(request.getId(), request.getStatus())
                );
            }

            RequestStatus dtoStatus = RequestStatus.valueOf(dto.status());

            if (dtoStatus == RequestStatus.CONFIRMED) {
                if (participantLimit > 0 && confirmedCount >= participantLimit) {
                    throw new ConflictException(
                            "Event %d has reached participant limit. Cannot confirm more requests (%d/%d)"
                                    .formatted(dto.eventId(), confirmedCount, participantLimit)
                    );
                }

                request.setStatus(RequestStatus.CONFIRMED);
                toConfirm.add(request);
                confirmedCount++;

            } else if (dtoStatus == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                toReject.add(request);
            }
        }

        List<ParticipationRequestDto> confirmedDtos = toConfirm.stream()
                .map(requestMapper::toDto)
                .toList();

        List<ParticipationRequestDto> rejectedDtos = toReject.stream()
                .map(requestMapper::toDto)
                .toList();

        return new EventRequestStatusUpdateResult(confirmedDtos, rejectedDtos);
    }

    private ParticipationRequest findRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> throwNotFound(requestId, EntityName.REQUEST));
    }

    private UserDto findUserOrThrow(Long userId) {
        return userFeignClient.getUserDto(userId);
    }

    private EventDtoInternal findEventOrThrow(Long eventId) {
        return eventFeignClient.get(eventId);
    }
}