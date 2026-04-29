package ru.practicum.core.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.core.dto.user.NewUserRequest;
import ru.practicum.core.dto.user.UserDto;
import ru.practicum.core.dto.user.UserInfoProjection;
import ru.practicum.core.dto.user.UserShortDto;
import ru.practicum.core.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserDto toDto(User user);

    UserShortDto toDto(UserInfoProjection projection);

    @Mapping(target = "id", ignore = true)
    User toEntity(NewUserRequest request);
}
