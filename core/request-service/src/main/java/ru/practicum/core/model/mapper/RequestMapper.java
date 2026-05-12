package ru.practicum.core.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.core.dto.request.ParticipationRequestDto;
import ru.practicum.core.model.ParticipationRequest;
import ru.practicum.core.model.RequestStatus;

import static ru.practicum.core.utils.constants.AppConstants.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "created", dateFormat = DATE_TIME_FORMAT)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    ParticipationRequestDto toDto(ParticipationRequest request);

    @Named("statusToString")
    default String statusToString(RequestStatus status) {
        return status != null ? status.toString() : null;
    }
}
