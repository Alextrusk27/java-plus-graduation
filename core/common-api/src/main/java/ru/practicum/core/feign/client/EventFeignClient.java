package ru.practicum.core.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.core.dto.event.response.EventDtoInternal;
import ru.practicum.core.feign.FeignConfig;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.core.utils.constants.ServiceNames;

@FeignClient(name = ServiceNames.EVENT_SERVICE, path = ApiPaths.Internal.EVENTS, configuration = FeignConfig.class)
public interface EventFeignClient {

    @GetMapping("/{eventId}")
    EventDtoInternal get(@PathVariable Long eventId);
}
