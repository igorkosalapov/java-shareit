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

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userService.findByIdOrThrow(userId);

        Item item = ItemMapper.toModel(itemDto, owner, null);
        Item saved = itemRepository.create(item);

        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может обновлять статус вещи");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemRepository.update(item);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> findByOwner(Long ownerId) {
        return itemRepository.findByOwner(ownerId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.findAvailable(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }
}
