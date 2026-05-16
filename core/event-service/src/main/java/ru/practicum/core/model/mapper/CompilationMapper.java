package ru.practicum.core.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.core.dto.compilation.NewCompilationDto;
import ru.practicum.core.dto.compilation.response.CompilationDto;
import ru.practicum.core.model.Compilation;

@Mapper
public interface CompilationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto newCompilationDto);

    @Mapping(target = "events", ignore = true)
    CompilationDto toDto(Compilation compilation);
}
