package ru.practicum.core.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.service.UserService;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(ApiPaths.Internal.USERS)
@RequiredArgsConstructor
@Validated
@Slf4j
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/dto/{userId}")
    public UserDto getUserDto(@PathVariable @NotNull @Positive Long userId) {
        log.debug("Internal UserDto request: get user {}", userId);
        var result = userService.getUser(userId);
        log.debug("Internal UserDto response: user {} found", userId);
        return result;
    }

    @GetMapping("/dto/short")
    public List<UserShortDto> getUserShortDto(@RequestParam @NotEmpty Set<Long> userIds) {
        log.debug("Internal UserShortDto request: get users {}", userIds);
        var result = userService.getUserShort(userIds);
        log.debug("Internal UserShortDto response: users {} found", result);
        return result;
    }
}
