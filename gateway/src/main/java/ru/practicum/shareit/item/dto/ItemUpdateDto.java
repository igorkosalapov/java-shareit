package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Positive;

public record ItemUpdateDto(
        String name,
        String description,
        Boolean available,
        @Positive(message = "requestId must be positive")
        Long requestId
) {
}
