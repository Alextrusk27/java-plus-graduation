package ru.practicum.core.resilience;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.exception.ServiceUnavailableException;
import ru.practicum.core.feign.client.UserFeignClient;
import ru.practicum.core.utils.constants.ServiceNames;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClientService {
    private final UserFeignClient userClient;

    @Retry(name = ServiceNames.USER_SERVICE)
    @CircuitBreaker(name = ServiceNames.USER_SERVICE, fallbackMethod = "fallbackError")
    public UserShortDto getUserRequired(Long userId) {
        return userClient.getUserDto(userId);
    }

    @Retry(name = ServiceNames.USER_SERVICE)
    @CircuitBreaker(name = ServiceNames.USER_SERVICE, fallbackMethod = "fallbackDefaultDto")
    public UserShortDto getUserOptional(Long userId) {
        return userClient.getUserDto(userId);
    }

    @Retry(name = ServiceNames.USER_SERVICE)
    @CircuitBreaker(name = ServiceNames.USER_SERVICE, fallbackMethod = "fallbackDefaultDtoList")
    public List<UserShortDto> getUsersOptional(Set<Long> userIds) {
        return userClient.getUserShortDto(userIds);
    }

    private UserShortDto fallbackError(Long userId, Throwable t) {
        log.error("Error while processing UserFeignClient for userId={}. Error: {} - {}. Cause: {}",
                userId,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error",
                t.getCause() != null ? t.getCause().toString() : "N/A");

        if (isConnectionError(t)) {
            throw new ServiceUnavailableException("Users data temporarily unavailable. Please try again later.");
        }
        throw new RuntimeException(t);
    }

    private UserShortDto fallbackDefaultDto(Long userId, Throwable t) {
        log.warn("Error while processing UserFeignClient for userId={}, returning stub. Error: {} - {}",
                userId,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            return new UserShortDto(userId, "NO DATA");
        }
        throw new RuntimeException(t);
    }

    private List<UserShortDto> fallbackDefaultDtoList(Set<Long> userIds, Throwable t) {
        log.warn("Error while processing UserFeignClient for userIds={}, returning stubs. Error: {} - {}",
                userIds,
                t.getClass().getSimpleName(),
                t.getMessage() != null ? t.getMessage() : "Unknown error");

        if (isConnectionError(t)) {
            return userIds.stream()
                    .map(id -> new UserShortDto(id, "NO DATA"))
                    .toList();
        }
        throw new RuntimeException(t);
    }

    private boolean isConnectionError(Throwable t) {
        return t instanceof feign.RetryableException ||
                t instanceof java.net.ConnectException ||
                t instanceof java.net.SocketTimeoutException;
    }
}
