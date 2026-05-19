package ru.practicum.core.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public UserShortDto getUser(@PathVariable @NotNull @Positive Long userId) {
        log.info("Internal request: get user {}", userId);
        var result = userService.getUser(userId);
        log.debug("Internal response: user {} found", userId);
        return result;
    }

    @GetMapping("/dto/list")
    public List<UserShortDto> getUser(@RequestParam @NotEmpty Set<Long> userIds) {
        log.info("Internal request: get users {}", userIds);
        var result = userService.getUser(userIds);
        log.debug("Internal response: users {} found", result);
        return result;
    }
}
