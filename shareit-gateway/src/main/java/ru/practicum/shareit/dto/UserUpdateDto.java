package ru.practicum.shareit.dto;

import jakarta.validation.constraints.Email;

public record UserUpdateDto(
        String name,
        @Email(message = "email must be valid")
        String email
) {}
