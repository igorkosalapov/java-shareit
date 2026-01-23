package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateDto(
        @NotBlank(message = "text must not be blank")
        String text
) {
}
