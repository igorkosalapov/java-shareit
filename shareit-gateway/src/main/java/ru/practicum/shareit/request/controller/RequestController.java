package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;


@RestController
@RequestMapping("/requests")
@Validated
public class RequestController {

    private final RequestClient requestClient;

    public RequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId) {
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId) {
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                          @PathVariable @Positive long requestId) {
        return requestClient.getById(userId, requestId);
    }
}
