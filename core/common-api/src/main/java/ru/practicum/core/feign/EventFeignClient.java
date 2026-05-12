package ru.practicum.core.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.utils.ApiPaths;

@FeignClient(name = "event-service", path = ApiPaths.Internal.EVENTS)
public interface EventFeignClient {

    @GetMapping("/{eventId}")
    EventDtoInternal get(@PathVariable Long eventId);
}
