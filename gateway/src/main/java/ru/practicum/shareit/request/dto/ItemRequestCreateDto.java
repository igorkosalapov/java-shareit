package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemRequestCreateDto(
        @NotBlank(message = "description must not be blank")
        String description
) {
}
