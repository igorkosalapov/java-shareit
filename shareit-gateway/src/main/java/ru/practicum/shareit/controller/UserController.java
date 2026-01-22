package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.client.UserClient;
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserUpdateDto;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserCreateDto dto) {
        return userClient.create(dto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable @Positive long userId,
                                         @RequestBody @Valid UserUpdateDto dto) {

        if (dto.name() != null && dto.name().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "name must not be blank");
        }
        if (dto.email() != null && dto.email().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "email must not be blank");
        }
        return userClient.update(userId, dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable @Positive long userId) {
        return userClient.getById(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteById(@PathVariable @Positive long userId) {
        return userClient.deleteById(userId);
    }
}
