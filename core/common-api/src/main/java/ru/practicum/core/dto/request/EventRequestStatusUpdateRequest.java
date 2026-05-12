package ru.practicum.core.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.practicum.core.dto.TEMPORARY.RequestStatus;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        @NotEmpty List<Long> requestIds,
        @NotNull RequestStatus status
) {
}
