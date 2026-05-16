package ru.practicum.core.service;

import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.dto.TEMPORARY.RequestStatus;
import ru.practicum.core.dto.TEMPORARY.StateAction;
import ru.practicum.core.dto.event.params.AdminSearchParams;
import ru.practicum.core.dto.event.params.EventParams;
import ru.practicum.core.dto.event.params.EventParamsSorted;
import ru.practicum.core.dto.event.params.PublicSearchParams;
import ru.practicum.core.dto.event.request.CreateEventDto;
import ru.practicum.core.dto.event.request.UpdateEventDto;
import ru.practicum.core.dto.event.response.*;
import ru.practicum.core.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.dto.request.UpdateRequestStatusDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.exception.AccessException;
import ru.practicum.core.exception.ConflictException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.model.Category;
import ru.practicum.core.model.Event;
import ru.practicum.core.model.State;
import ru.practicum.core.model.mapper.EventMapper;
import ru.practicum.core.repository.CategoryRepository;
import ru.practicum.core.repository.EventPredicateBuilder;
import ru.practicum.core.repository.EventRepository;
import ru.practicum.core.resilience.RequestClientService;
import ru.practicum.core.resilience.UserClientService;
import ru.practicum.core.utils.BaseService;
import ru.practicum.core.utils.EntityName;
import ru.practicum.ewm.CreateHitDto;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.stats.client.StatsClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.core.utils.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl extends BaseService implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final EventMapper mapper;

    private final UserClientService userClientService;
    private final RequestClientService requestClientService;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto create(CreateEventDto dto) {
        UserShortDto initiator = userClientService.getUserRequired(dto.initiatorId());
        Category category = findCategoryOrThrow(dto.category());

        Event event = mapper.toEntity(dto);
        event.setCategory(category);

        event = eventRepository.save(event);

        return mapper.toDto(event, initiator);
    }

    @Transactional
    @Override
    public EventDto update(UpdateEventDto dto) {

        UserShortDto initiator = userClientService.getUserRequired(dto.initiatorId());
        Event event = findEventOrThrow(dto.eventId());

        if (!initiator.id().equals(dto.initiatorId())) {
            throw new AccessException("Only initiator can update event");
        }

        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        mapper.updateEntity(dto, event);

        if (categoryChanged(event, dto)) {
            Category category = findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        if (dto.hasStateAction()) {
            if (dto.stateAction() == StateAction.PUBLISH_EVENT ||
                    (dto.stateAction() == StateAction.REJECT_EVENT)) {
                throw new IllegalArgumentException("Illegal private state action: %s".formatted(dto.stateAction()));
            }
            applyStateAction(event, dto.stateAction());
        }

        return mapper.toDto(event, initiator);
    }

    @Override
    @Transactional
    public EventDto adminUpdate(UpdateEventDto dto) {
        Event event = findEventOrThrow(dto.eventId());

        UserShortDto initiator = userClientService.getUserRequired(event.getInitiatorId());
        mapper.updateEntity(dto, event);

        if (dto.hasStateAction()) {
            if ((dto.stateAction() == StateAction.PUBLISH_EVENT ||
                    dto.stateAction() == StateAction.REJECT_EVENT) &&
                    event.getState() != State.PENDING) {

                throw new ConflictException("Cannot publish/reject the event because it's not in the right state: %s"
                        .formatted(event.getState()));

            }
            if (dto.stateAction() == StateAction.SEND_TO_REVIEW ||
                    dto.stateAction() == StateAction.CANCEL_REVIEW) {
                throw new IllegalArgumentException("Illegal admin state action: %s".formatted(dto.stateAction()));
            }

            applyStateAction(event, dto.stateAction());
        }

        if (categoryChanged(event, dto)) {
            Category category = findCategoryOrThrow(dto.category());
            event.setCategory(category);
        }

        return mapper.toDto(event, initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public EventDtoExtended get(Long id) {
        Event event = findEventOrThrow(id);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException(String.format("Event with Id=%d was not found", id));
        }

        createHit(createUri(id));

        Long views = getStat(List.of(event))
                .getOrDefault(event.getId(), 0L);

        Long confirmedRequests = requestClientService
                .countByEvent(id, RequestStatus.CONFIRMED.toString());

        UserShortDto initiator = userClientService.getUserOptional(event.getInitiatorId());

        return mapper.toExtendedDto(event, views, confirmedRequests, initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDtoShortWithoutViews> get(EventParamsSorted params) {

        List<Event> events = eventRepository.findByInitiatorId(
                        params.userId(),
                        params.pageable())
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestClientService
                .countByEvents(eventsIds, RequestStatus.CONFIRMED.toString());

        UserShortDto initiator = userClientService.getUserOptional(params.userId());

        return events.stream()
                .map(event -> mapper.toDtoShort(
                        event,
                        confirmedRequests.get(event.getId()),
                        initiator))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventDtoExtended get(EventParams params) {
        Event event = findEventOrThrow(params.eventId());
        UserShortDto initiator = userClientService.getUserOptional(params.userId());

        return mapper.toExtendedDto(
                event,
                getStat(List.of(event)).getOrDefault(params.eventId(), 0L),
                requestClientService
                        .countByEvent(event.getId(), RequestStatus.CONFIRMED.toString()),
                initiator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDtoExtended> get(AdminSearchParams params) {
        List<State> states = params.states().stream()
                .map(State::valueOf)
                .toList();

        Predicate predicate = new EventPredicateBuilder()
                .withInitiators(params.users())
                .withStates(states)
                .withCategories(params.categories())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestClientService
                .countByEvents(eventsIds, RequestStatus.CONFIRMED.toString());

        Map<Long, Long> views = getStat(events);

        List<UserShortDto> initiatorsDto = userClientService.getUsersOptional(
                events.stream()
                        .map(Event::getInitiatorId)
                        .collect(Collectors.toSet())
        );

        Map<Long, UserShortDto> initiators = initiatorsDto.stream()
                .collect(Collectors.toMap(
                        UserShortDto::id, Function.identity()
                ));

        return events.stream()
                .map(event -> mapper.toExtendedDto(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        initiators.get(event.getInitiatorId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDtoShort> get(PublicSearchParams params) {
        Predicate predicate = new EventPredicateBuilder()
                .withTextSearch(params.text())
                .withCategories(params.categories())
                .withPaid(params.paid())
                .withDateRange(params.rangeStart(), params.rangeEnd())
                .forPublicSearch()
                .build();

        List<Event> events = eventRepository.findAll(predicate, params.pageable())
                .getContent();

        createHit(EVENTS_BASE_PATH);

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestClientService
                .countByEvents(eventsIds, RequestStatus.CONFIRMED.toString());

        List<UserShortDto> initiatorsDto = userClientService.getUsersOptional(
                events.stream()
                        .map(Event::getInitiatorId)
                        .collect(Collectors.toSet()));

        Map<Long, UserShortDto> initiators = initiatorsDto.stream()
                .collect(Collectors.toMap(
                        UserShortDto::id, Function.identity()
                ));

        events = filterAvailableEvents(events, confirmedRequests);

        Map<Long, Long> views = getStat(events);

        if (params.sort().equals(Sort.VIEWS)) {
            events.sort(Comparator.comparing(
                    event -> views.getOrDefault(event.getId(), 0L),
                    Comparator.reverseOrder()
            ));
        }

        return events.stream()
                .map(event -> mapper.toDtoShort(
                        event,
                        views.getOrDefault(event.getId(), 0L),
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        initiators.get(event.getInitiatorId())))
                .toList();
    }

    @Override
    public EventDtoInternal getInternalEvent(Long eventId) {
        return mapper.toDtoInternal(findEventOrThrow(eventId));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(EventParams params) {
        Event event = findEventOrThrow(params.eventId());

        if (!event.getInitiatorId().equals(params.userId())) {
            throw new AccessException(
                    "User %d attempted to view requests for event %d, but is not the initiator"
                            .formatted(params.userId(), params.eventId()));
        }

        return requestClientService.getByEvent(event.getId(), REQUESTS_DEFAULT_PAGEABLE);
    }

    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(UpdateRequestStatusDto dto) {
        Event event = findEventOrThrow(dto.eventId());

        if (!event.getInitiatorId().equals(dto.userId())) {
            throw new AccessException(
                    """
                            User %d is not authorized to manage requests for event %d.
                            Only event initiator can perform this action.
                            """.formatted(dto.userId(), dto.eventId())
            );
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException(
                    "Event %d has request moderation disabled. Cannot update request statuses"
                            .formatted(dto.eventId())
            );
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException(
                    "Cannot update request statuses for event %d: event is not published (current state: %s)"
                            .formatted(dto.eventId(), event.getState())
            );
        }

        return requestClientService.updateStatuses(dto, event.getParticipantLimit());
    }

    private Event findEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> throwNotFound(eventId, EntityName.EVENT));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> throwNotFound(categoryId, EntityName.CATEGORY));
    }

    private Map<Long, Long> getStat(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> uris = events.stream()
                .map(p -> EVENT_PATH_TEMPLATE + p.getId())
                .toList();

        LocalDateTime minDataTime = events.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.now());

        List<ViewStatsDto> stats = statsClient.getStats(
                        minDataTime.format(DATE_TIME_FORMATTER),
                        LocalDateTime.now().format(DATE_TIME_FORMATTER),
                        uris,
                        true)
                .getBody();

        if (stats == null) {
            return Collections.emptyMap();
        }

        return stats.stream().collect(Collectors.toMap(
                view -> extractIdFromUri(view.uri()),
                ViewStatsDto::hits
        ));
    }

    private void createHit(String uri) {
        try {
            CreateHitDto dto = new CreateHitDto(
                    MAIN_APP_NAME,
                    uri,
                    InetAddress.getLocalHost().getHostAddress(),
                    LocalDateTime.now());

            statsClient.createHit(dto);

        } catch (UnknownHostException e) {
            throw new RuntimeException("Error while creating hit.", e);
        }
    }

    private String createUri(Long eventId) {
        return EVENT_PATH_TEMPLATE + eventId;
    }

    private Long extractIdFromUri(String uri) {
        String[] split = uri.split("/");
        String lastPart = split[split.length - 1];
        return Long.parseLong(lastPart);
    }

    private boolean categoryChanged(Event event, UpdateEventDto dto) {
        Long dtoCategoryId = dto.category();

        if (dtoCategoryId == null) {
            return false;
        }

        Long eventCategoryId = event.getCategory().getId();
        return !eventCategoryId.equals(dtoCategoryId);
    }

    private void applyStateAction(Event event, StateAction stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT -> {
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            case REJECT_EVENT, CANCEL_REVIEW -> event.setState(State.CANCELED);

            case SEND_TO_REVIEW -> event.setState(State.PENDING);
            default -> throw new IllegalArgumentException("Unacceptable state action: " + stateAction);
        }
    }

    private List<Event> filterAvailableEvents(List<Event> events, Map<Long, Long> confirmedRequests) {
        return events.stream()
                .filter(event -> {
                    long requestsCount = confirmedRequests.getOrDefault(event.getId(), 0L);
                    return requestsCount < event.getParticipantLimit();
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}