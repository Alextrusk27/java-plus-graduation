package ru.practicum.core.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;
import ru.practicum.core.feign.FeignConfig;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = ApiPaths.Internal.REQUESTS, configuration = FeignConfig.class)
public interface RequestFeignClient {

    @GetMapping("/count/{eventId}")
    Long countRequestsByEventAndStatus(@PathVariable Long eventId,
                                       @RequestParam String status);

    @GetMapping("/count/events")
    Map<Long, Long> countRequestsByEventsAndStatus(@RequestParam List<Long> eventIds,
                                                   @RequestParam String status);

    @GetMapping("{eventId}")
    List<ParticipationRequestDto> getByEventId(@PathVariable Long eventId,
                                               @SpringQueryMap Pageable pageable);

    @PostMapping("/update/statuses")
    EventRequestStatusUpdateResult updateStatuses(@RequestBody UpdateRequestStatusDto dto,
                                                  @RequestParam Integer participantLimit);
}
