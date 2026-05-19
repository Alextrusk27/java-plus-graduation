package ru.practicum.core.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.core.model.QParticipationRequest;
import ru.practicum.core.model.RequestStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RequestRepositoryCustomImpl implements RequestRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> countByEventIdsAndStatus(List<Long> eventIds, RequestStatus status) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        QParticipationRequest request = QParticipationRequest.participationRequest;

        List<Tuple> results = queryFactory
                .select(request.eventId, request.count())
                .from(request)
                .where(
                        request.eventId.in(eventIds)
                                .and(request.status.eq(status))
                )
                .groupBy(request.eventId)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(request.eventId),
                        tuple -> Optional.ofNullable(tuple.get(request.count()))
                                .orElse(0L)
                ));
    }
}
