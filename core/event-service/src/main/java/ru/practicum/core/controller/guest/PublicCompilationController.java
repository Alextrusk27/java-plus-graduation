package ru.practicum.core.controller.guest;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.compilation.response.CompilationDto;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.core.service.CompilationService;

import java.util.List;

@Validated
@RestController
@RequestMapping(ApiPaths.Public.COMPILATIONS)
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("PUBLIC: Get COMPILATIONS with params: pinned '{}', from '{}', size '{}'", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compilationId}")
    public CompilationDto getCompilationById(@PathVariable Long compilationId) {
        log.info("PUBLIC: Get COMPILATION {}", compilationId);
        return compilationService.getCompilationById(compilationId);
    }
}
