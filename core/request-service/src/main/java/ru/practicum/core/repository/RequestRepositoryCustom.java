package ru.practicum.core.repository;

import ru.practicum.core.model.RequestStatus;

import java.util.List;
import java.util.Map;

public interface RequestRepositoryCustom {

    Map<Long, Long> countByEventIdsAndStatus(List<Long> eventIds, RequestStatus status);
}
