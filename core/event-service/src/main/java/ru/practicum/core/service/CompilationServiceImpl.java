package ru.practicum.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.dto.TEMPORARY.RequestStatus;
import ru.practicum.core.dto.compilation.NewCompilationDto;
import ru.practicum.core.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.dto.compilation.response.CompilationDto;
import ru.practicum.core.dto.event.response.EventDtoShortWithoutViews;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.feign.client.RequestFeignClient;
import ru.practicum.core.feign.client.UserFeignClient;
import ru.practicum.core.model.Compilation;
import ru.practicum.core.model.Event;
import ru.practicum.core.model.mapper.CompilationMapper;
import ru.practicum.core.model.mapper.EventMapper;
import ru.practicum.core.repository.CompilationRepository;
import ru.practicum.core.repository.EventRepository;
import ru.practicum.core.utils.BaseService;
import ru.practicum.core.utils.EntityName;
import ru.practicum.core.utils.PageableFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl extends BaseService implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    private final RequestFeignClient requestFeignClient;
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.events() != null && !newCompilationDto.events().isEmpty()) {
            compilation.setEvents(eventRepository
                    .findAllByIdIn(newCompilationDto.events()));
        }

        compilationRepository.save(compilation);

        List<Long> eventsIds = compilation
                .getEvents()
                .stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestFeignClient
                .countRequestsByEventsAndStatus(eventsIds, RequestStatus.CONFIRMED.toString());

        List<EventDtoShortWithoutViews> eventsWithRequests = new ArrayList<>();

        if (!compilation.getEvents().isEmpty()) {
            Map<Long, UserShortDto> initiators = getEventsInitiators(compilation.getEvents());

            eventsWithRequests = compilation.getEvents().stream()
                    .map(event -> eventMapper.toDtoShort(
                            event, confirmedRequests
                                    .getOrDefault(event.getId(), 0L),
                            initiators.get(event.getInitiatorId())))
                    .toList();
        }

        return new CompilationDto(compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                eventsWithRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = findCompilationOrThrow(compilationId);

        List<EventDtoShortWithoutViews> preparedEvents = prepareEvents(compilation);

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size, Sort.by("id"));

        List<Compilation> compilations = compilationRepository
                .findAllByPinned(pinned, pageable)
                .getContent();

        if (compilations.isEmpty()) {
            return List.of();
        }

        List<Long> eventsIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, Long> confirmedRequests = eventsIds.isEmpty()
                ? Collections.emptyMap()
                : requestFeignClient.countRequestsByEventsAndStatus(eventsIds, RequestStatus.CONFIRMED.toString());

        Map<Long, UserShortDto> initiators = eventsIds.isEmpty()
                ? Collections.emptyMap()
                : getEventsInitiators(compilations.stream()
                .filter(compilation -> compilation.getEvents() != null && !compilation.getEvents().isEmpty())
                .flatMap(c -> c.getEvents().stream())
                .collect(Collectors.toSet()));

        return compilations.stream()
                .map(compilation -> new CompilationDto(
                        compilation.getId(),
                        compilation.getPinned(),
                        compilation.getTitle(),
                        compilation.getEvents() != null ? compilation.getEvents().stream()
                                .map(event -> eventMapper.toDtoShort(
                                        event,
                                        confirmedRequests.getOrDefault(event.getId(), 0L),
                                        initiators.get(event.getInitiatorId())))
                                .toList() : List.of()))
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation) {
        Compilation compilation = findCompilationOrThrow(compId);

        if (updateCompilation.events() != null) {
            Set<Event> events = updateCompilation.events().stream()
                    .map(id ->
                            eventRepository.findById(id).orElseThrow(()
                                    -> new NotFoundException(String.format("Event with Id=%d was not found", id))))
                    .collect(Collectors.toSet());
            compilation.setEvents(events);
        }

        if (updateCompilation.pinned() != null) {
            compilation.setPinned(updateCompilation.pinned());
        }

        String title = updateCompilation.title();
        if (title != null && !title.isBlank()) {
            compilation.setTitle(title);
        }

        List<EventDtoShortWithoutViews> preparedEvents = prepareEvents(compilation);

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        findCompilationOrThrow(compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private Compilation findCompilationOrThrow(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> throwNotFound(compilationId, EntityName.COMPILATION));
    }

    private List<EventDtoShortWithoutViews> prepareEvents(Compilation compilation) {
        Set<Event> events = compilation.getEvents();

        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventsIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestFeignClient
                .countRequestsByEventsAndStatus(eventsIds, RequestStatus.CONFIRMED.toString());

        Map<Long, UserShortDto> initiators = getEventsInitiators(events);

        return events.stream()
                .map(event -> eventMapper.toDtoShort(
                        event,
                        confirmedRequests.getOrDefault(event.getId(), 0L),
                        initiators.get(event.getInitiatorId())))
                .toList();
    }

    private Map<Long, UserShortDto> getEventsInitiators(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        return userFeignClient.getUserShortDto(
                        events.stream()
                                .map(Event::getInitiatorId)
                                .collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(UserShortDto::id, Function.identity()));
    }
}
