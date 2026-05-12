package ru.practicum.core.service;

import ru.practicum.core.dto.user.request.NewUserRequest;
import ru.practicum.core.dto.user.request.UserSearchRequest;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.response.UserShortDto;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserDto> getUsers(UserSearchRequest request);

    UserDto getUser(Long userId);

    List<UserShortDto> getUserShort(Set<Long> userIds);

    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);
}
