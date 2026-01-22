package ru.practicum.shareit.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestResponseDto(
        Long id,
        String description,
        LocalDateTime created,
        List<ItemForRequestDto> items
) {
}
