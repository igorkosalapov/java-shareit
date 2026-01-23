package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequest toEntity(String description, User requestor, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);
        return request;
    }

    public ItemRequestResponseDto toResponse(ItemRequest req, List<ItemForRequestDto> items) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(req.getId());
        dto.setDescription(req.getDescription());
        dto.setCreated(req.getCreated());
        dto.setItems(items == null ? Collections.emptyList() : items);
        return dto;
    }

    public ItemForRequestDto toItemForRequestDto(Item item) {
        ItemForRequestDto dto = new ItemForRequestDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setOwnerId(item.getOwner().getId());
        return dto;
    }
}


