package ru.practicum.core.model.mapper;

import org.mapstruct.*;
import ru.practicum.core.dto.event.response.*;
import ru.practicum.core.dto.event.request.CreateEventDto;
import ru.practicum.core.dto.event.request.UpdateEventDto;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.model.Event;

import static ru.practicum.core.model.State.DEFAULT_STATE;

@Mapper(uses = {
        LocationMapper.class,
        CategoryMapper.class
})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", constant = DEFAULT_STATE)
    @Mapping(target = "requestModeration", source = "requestModeration", defaultValue = "true")
    @Mapping(target = "paid", source = "paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "participantLimit", defaultValue = "0")
    @Mapping(target = "location", source = "location")
    Event toEntity(CreateEventDto dto);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    EventDto toDto(Event event, UserDto initiator);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator", source = "initiator")
    EventDtoExtended toExtendedDto(Event event, Long views, Long confirmedRequests, UserShortDto initiator);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator", source = "initiator")
    EventDtoShort toDtoShort(Event event, Long views, Long confirmedRequests, UserShortDto initiator);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "initiator", source = "initiator")
    EventDtoShortWithoutViews toDtoShort(Event event, Long confirmedRequests, UserShortDto initiator);

    EventDtoInternal toDtoInternal(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "location", source = "location")
    void updateEntity(UpdateEventDto dto, @MappingTarget Event event);
}
