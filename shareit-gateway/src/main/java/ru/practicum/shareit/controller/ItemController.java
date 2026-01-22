package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.dto.CommentCreateDto;
import ru.practicum.shareit.dto.ItemCreateDto;
import ru.practicum.shareit.dto.ItemUpdateDto;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemClient itemClient;

    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                         @Valid @RequestBody ItemCreateDto dto) {
        return itemClient.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                         @PathVariable @Positive long itemId,
                                         @Valid @RequestBody ItemUpdateDto dto) {

        if (dto.name() != null && dto.name().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "name must not be blank");
        }
        if (dto.description() != null && dto.description().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "description must not be blank");
        }
        return itemClient.update(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                          @PathVariable @Positive long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        return itemClient.getOwnerItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "10") @Min(1) int size) {

        if (text == null || text.isBlank()) {
            return ResponseEntity.ok().body(java.util.List.of());
        }
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(BaseClient.USER_HEADER) long userId,
            @PathVariable long itemId,
            @Valid @RequestBody CommentCreateDto dto
    ) {
        return itemClient.addComment(userId, itemId, dto);
    }

}
