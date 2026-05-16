package ru.practicum.core.service;

import ru.practicum.core.dto.compilation.NewCompilationDto;
import ru.practicum.core.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.dto.compilation.response.CompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto getCompilationById(Long compilationId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation);

    void deleteCompilation(Long compilationId);
}
