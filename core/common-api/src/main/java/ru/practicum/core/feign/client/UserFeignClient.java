package ru.practicum.core.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.core.dto.user.response.UserShortDto;
import ru.practicum.core.feign.FeignConfig;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.core.utils.constants.ServiceNames;

import java.util.List;
import java.util.Set;

@FeignClient(name = ServiceNames.USER_SERVICE, path = ApiPaths.Internal.USERS, configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/dto/{userId}")
    UserShortDto getUserDto(@PathVariable Long userId);

    @GetMapping("/dto/list")
    List<UserShortDto> getUserShortDto(@RequestParam Set<Long> userIds);
}
