package ru.practicum.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.core.model.ParticipationRequest;
import ru.practicum.core.model.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long>,
        QuerydslPredicateExecutor<ParticipationRequest>,
        RequestRepositoryCustom {

    Boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    Page<ParticipationRequest> findAllByEventId(Long eventId, Pageable pageable);
}
