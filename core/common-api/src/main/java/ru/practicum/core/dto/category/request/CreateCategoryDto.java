package ru.practicum.core.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryDto(
        @Size(min = 1, max = 50)
        @NotBlank
        String name
) {
}
