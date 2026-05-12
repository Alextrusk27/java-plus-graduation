package ru.practicum.core.dto.category.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDto(
        Long id,

        @Size(min = 1, max = 50)
        @NotBlank
        String name
) {
}
