package ru.practicum.core.service;

import ru.practicum.core.dto.user.NewUserRequest;
import ru.practicum.core.dto.user.UserDto;
import ru.practicum.core.dto.user.UserSearchRequest;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(UserSearchRequest request);

    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);
}
