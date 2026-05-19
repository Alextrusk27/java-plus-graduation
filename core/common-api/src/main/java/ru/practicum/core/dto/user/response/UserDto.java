package ru.practicum.core.dto.user.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        Long id,

        @NotBlank(message = "Имя не должно быть пустым")
        String name,

        @Email(message = "Некорректный email")
        @NotBlank(message = "Email не может быть пустым")
        String email
) {
}
