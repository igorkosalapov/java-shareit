package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userService.findByIdOrThrow(userId);

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        Item saved = itemRepository.create(item);
        return itemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может обновлять статус вещи");
        }

        itemMapper.update(itemDto, item);
        return itemMapper.toDto(item);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> findByOwner(Long ownerId) {
        return itemMapper.toDtoList(itemRepository.findByOwner(ownerId));
    }

    @Override
    public List<ItemDto> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemMapper.toDtoList(itemRepository.findAvailable(text));
    }
}
