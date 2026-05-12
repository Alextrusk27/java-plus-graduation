package ru.practicum.core.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.dto.CompilationDto;
import ru.practicum.core.dto.NewCompilationDto;
import ru.practicum.core.dto.UpdateCompilationRequest;
import ru.practicum.core.utils.ApiPaths;
import ru.practicum.core.service.CompilationService;

@RestController
@RequestMapping(ApiPaths.Admin.COMPILATIONS)
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("ADMIN: Create COMPILATION {}", newCompilationDto);
        return compilationService.addCompilation(newCompilationDto);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto updateCompilation(@PathVariable Long compilationId,
                                            @RequestBody @Valid UpdateCompilationRequest updateCompilation) {
        log.info("ADMIN: Update COMPILATION {}", updateCompilation);
        return compilationService.updateCompilation(compilationId, updateCompilation);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compilationId) {
        log.info("ADMIN: Delete COMPILATION {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }
}
