package ru.practicum.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.core.model.Event;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>,
        QuerydslPredicateExecutor<Event> {

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    Set<Event> findAllByIdIn(List<Long> events);
}
