package ru.practicum.core.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;
import ru.practicum.core.service.RequestService;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiPaths.Internal.REQUESTS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class InternalRequestController {
    private final RequestService requestService;

    @GetMapping("/count/{eventId}")
    public Long countRequestsByEventAndStatus(@PathVariable @NotNull @Positive Long eventId,
                                              @RequestParam @NotBlank String status) {

        log.info("Internal request: count confirmed requests by event {}", eventId);
        var result = requestService.countByEventAndStatus(eventId, status);
        log.debug("Internal response: {} confirmed requests found in event {}", result, eventId);
        return result;
    }

    @GetMapping("/count/events")
    public Map<Long, Long> countRequestsByEventsAndStatus(@RequestParam @NotEmpty List<Long> eventIds,
                                                          @RequestParam @NotBlank String status) {

        log.info("Internal request: count confirmed requests by events {}", eventIds);
        var result = requestService.countByEventsAndStatus(eventIds, status);
        log.debug("Internal response: confirmed requests found {}", result);
        return result;
    }

    @GetMapping("{eventId}")
    public List<ParticipationRequestDto> getByEventId(@PathVariable @NotNull @Positive Long eventId,
                                                      Pageable pageable) {

        log.info("Internal request: get requests by event {}", eventId);
        var result = requestService.getByEventId(eventId, pageable);
        log.debug("Internal response: found {} for event {}", result.size(), eventId);
        return result;
    }

    @PostMapping("/update/statuses")
    public EventRequestStatusUpdateResult updateStatuses(@RequestBody @NotNull UpdateRequestStatusDto dto,
                                                         @RequestParam @NotNull Integer participantLimit) {

        log.info("Internal request: update requests by event {}", dto.eventId());
        var result = requestService.updateRequestsStatus(dto, participantLimit);
        log.debug("Internal response: confirmed {} rejected {} requests for event {}",
                result.confirmedRequests().size(), result.rejectedRequests().size(), dto.eventId());
        return result;
    }
}
