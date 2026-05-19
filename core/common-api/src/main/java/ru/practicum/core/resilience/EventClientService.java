package ru.practicum.core.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.exception.ServiceUnavailableException;
import ru.practicum.core.feign.client.EventFeignClient;
import ru.practicum.core.utils.constants.ServiceNames;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventClientService {
    private final EventFeignClient eventClient;

    @Retry(name = ServiceNames.EVENT_SERVICE)
    @CircuitBreaker(name = ServiceNames.EVENT_SERVICE, fallbackMethod = "fallbackGetEvent")
    public EventDtoInternal getEventRequired(Long eventId) {
        return eventClient.get(eventId);
    }

    private EventDtoInternal fallbackGetEvent(Long eventId, Throwable t) {
        log.error("Error while processing EventFeignClient for eventId={}. Error: {} - {}. Cause: {}",
                eventId,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error",
                t.getCause() != null ? t.getCause().toString() : "N/A");

        if (isConnectionError(t)) {
            throw new ServiceUnavailableException("Events data temporarily unavailable. Please try again later.");
        }
        throw new RuntimeException(t);
    }

    private boolean isConnectionError(Throwable t) {
        return t instanceof feign.RetryableException ||
                t instanceof java.net.ConnectException ||
                t instanceof java.net.SocketTimeoutException;
    }
}
