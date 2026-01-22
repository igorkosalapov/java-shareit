package ru.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemCreateDto(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "description must not be blank")
        String description,
        @NotNull(message = "available must not be null")
        Boolean available,
        @Positive(message = "requestId must be positive")
        Long requestId
) {
}
