package ru.practicum.core.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;
import ru.practicum.core.exception.ServiceUnavailableException;
import ru.practicum.core.feign.client.RequestFeignClient;
import ru.practicum.core.utils.constants.ServiceNames;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestClientService {
    private final RequestFeignClient requestClient;

    @Retry(name = ServiceNames.REQUEST_SERVICE)
    @CircuitBreaker(name = ServiceNames.REQUEST_SERVICE, fallbackMethod = "countFallback")
    public Long countByEvent(Long eventId, String status) {
        return requestClient.countRequestsByEventAndStatus(eventId, status);
    }

    @Retry(name = ServiceNames.REQUEST_SERVICE)
    @CircuitBreaker(name = ServiceNames.REQUEST_SERVICE, fallbackMethod = "countMapFallback")
    public Map<Long, Long> countByEvents(List<Long> eventIds, String status) {
        return requestClient.countRequestsByEventsAndStatus(eventIds, status);
    }

    @Retry(name = ServiceNames.REQUEST_SERVICE)
    @CircuitBreaker(name = ServiceNames.REQUEST_SERVICE, fallbackMethod = "getByEventFallback")
    public List<ParticipationRequestDto> getByEvent(Long eventId, Pageable pageable) {
        return requestClient.getByEventId(eventId, pageable);
    }

    @Retry(name = ServiceNames.REQUEST_SERVICE)
    @CircuitBreaker(name = ServiceNames.REQUEST_SERVICE, fallbackMethod = "updateStatusesFallback")
    public EventRequestStatusUpdateResult updateStatuses(UpdateRequestStatusDto dto, Integer participantLimit) {
        return requestClient.updateStatuses(dto, participantLimit);
    }

    private Long countFallback(Long eventId, String status, Throwable t) {
        log.warn("Error while processing RequestFeignClient for eventId={} status={}, returning 0. Error: {} - {}",
                eventId, status,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            return 0L;
        }
        throw new RuntimeException(t);
    }

    private Map<Long, Long> countMapFallback(List<Long> eventIds, String status, Throwable t) {
        log.warn("Error while processing RequestFeignClient for eventIds={} status={}, returning zeros. Error: {} - {}",
                eventIds, status,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            return eventIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> 0L));
        }
        throw new RuntimeException(t);
    }

    private List<ParticipationRequestDto> getByEventFallback(Long eventId, Pageable ignored, Throwable t) {
        log.warn("Error while processing RequestFeignClient for eventId={}, returning empty list. Error: {} - {}",
                eventId,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            return List.of();
        }
        throw new RuntimeException(t);
    }

    private EventRequestStatusUpdateResult updateStatusesFallback(UpdateRequestStatusDto dto, Integer participantLimit,
                                                                  Throwable t) {
        log.error("Error while processing RequestFeignClient for status update. eventId={}, participantLimit={}. " +
                        "Error: {} - {}",
                dto.eventId(),
                participantLimit,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            throw new ServiceUnavailableException("Requests data temporarily unavailable. Please try again later.");
        }
        throw new RuntimeException(t);
    }

    private boolean isConnectionError(Throwable t) {
        return t instanceof feign.RetryableException ||
                t instanceof java.net.ConnectException ||
                t instanceof java.net.SocketTimeoutException;
    }
}