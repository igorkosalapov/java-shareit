package ru.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemRequestCreateDto(
        @NotBlank(message = "description must not be blank")
        String description
) {
}
