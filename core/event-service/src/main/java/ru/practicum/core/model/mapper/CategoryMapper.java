package ru.practicum.core.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.core.dto.category.request.CreateCategoryDto;
import ru.practicum.core.dto.category.response.CategoryDto;
import ru.practicum.core.model.Category;

@Mapper
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toEntity(CreateCategoryDto createCategoryDto);

    CategoryDto toDto(Category category);
}
