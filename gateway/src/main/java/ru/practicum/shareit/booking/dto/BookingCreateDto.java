package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record BookingCreateDto(
        @NotNull(message = "start must not be null")
        LocalDateTime start,
        @NotNull(message = "end must not be null")
        LocalDateTime end,
        @NotNull(message = "itemId must not be null")
        @Positive(message = "itemId must be positive")
        Long itemId
) {
}
