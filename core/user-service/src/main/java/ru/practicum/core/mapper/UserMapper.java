package ru.practicum.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.dto.user.request.NewUserRequest;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest request);
}
