package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.common.Headers.USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId) {
        return itemService.findById(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDto commentCreateDto
    ) {
        return itemService.addComment(userId, itemId, commentCreateDto);
    }

    @GetMapping
    public List<ItemDto> findByOwner(@RequestHeader(USER_ID) Long ownerId) {
        return itemService.findByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchAvailable(@RequestHeader(USER_ID) Long userId, @RequestParam String text) {
        return itemService.searchAvailable(text);
    }
}
