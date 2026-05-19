package ru.practicum.core.service;

import ru.practicum.core.dto.category.request.CreateCategoryDto;
import ru.practicum.core.dto.category.response.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CreateCategoryDto createCategoryDto);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    CategoryDto getCategoryById(Long id);

    void deleteCategory(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);
}
