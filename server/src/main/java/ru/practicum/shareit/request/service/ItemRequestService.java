package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto create(Long userId, ItemRequestCreateDto dto);

    List<ItemRequestResponseDto> getOwn(Long userId);

    List<ItemRequestResponseDto> getAllOthers(Long userId);

    ItemRequestResponseDto getById(Long userId, Long requestId);
}
