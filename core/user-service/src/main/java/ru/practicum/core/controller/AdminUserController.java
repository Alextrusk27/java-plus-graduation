package ru.practicum.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.user.request.NewUserRequest;
import ru.practicum.core.dto.user.request.UserSearchRequest;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.service.UserService;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.Admin.USERS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminUserController {
    private final UserService service;

    @GetMapping
    public List<UserDto> get(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("ADMIN: Get users with Ids {}", ids);
        UserSearchRequest request = UserSearchRequest.builder()
                .ids(ids)
                .from(from)
                .size(size)
                .build();
        log.info("ADMIN: Users with Ids {} found", ids);
        return service.getUsers(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest request) {
        log.info("ADMIN: Create user with request {}", request);
        UserDto result = service.createUser(request);
        log.info("ADMIN: Created user {}", result);
        return result;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("ADMIN: Delete user with userId {}", userId);
        service.deleteUser(userId);
        log.info("ADMIN: User with userId {} deleted", userId);
    }
}
