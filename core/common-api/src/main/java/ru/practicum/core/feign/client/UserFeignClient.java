package ru.practicum.core.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.dto.user.response.UserDto;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.feign.FeignConfig;
import ru.practicum.core.utils.ApiPaths;

import java.util.List;
import java.util.Set;

@FeignClient(name = "user-service", path = ApiPaths.Internal.USERS, configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/dto/{userId}")
    UserDto getUserDto(@PathVariable Long userId);

    @GetMapping("/dto/short")
    List<UserShortDto> getUserShortDto(@RequestParam Set<Long> userIds);
}
