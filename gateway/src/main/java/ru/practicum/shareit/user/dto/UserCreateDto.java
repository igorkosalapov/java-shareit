package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateDto(
        @NotBlank(message = "name must not be blank")
        String name,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email
) {}
