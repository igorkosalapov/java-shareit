package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemRequestMapper requestMapper;

    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestCreateDto dto) {
        User requestor = userService.findByIdOrThrow(userId);

        ItemRequest request = requestMapper.toEntity(dto.getDescription(), requestor, LocalDateTime.now());
        request = itemRequestRepository.save(request);
        return requestMapper.toResponse(request, Collections.emptyList());
    }

    @Override
    public List<ItemRequestResponseDto> getOwn(Long userId) {
        userService.findByIdOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId);
        return enrichWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllOthers(Long userId) {
        userService.findByIdOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(userId);
        return enrichWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        userService.findByIdOrThrow(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<ItemForRequestDto> items = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(requestMapper::toItemForRequestDto)
                .collect(Collectors.toList());

        return requestMapper.toResponse(request, items);
    }

    private List<ItemRequestResponseDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequest_IdIn(requestIds);

        Map<Long, List<ItemForRequestDto>> itemsByRequestId = items.stream()
                .filter(i -> i.getRequest() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getRequest().getId(),
                        Collectors.mapping(requestMapper::toItemForRequestDto, Collectors.toList())
                ));

        return requests.stream()
                .map(r -> requestMapper.toResponse(r, itemsByRequestId.getOrDefault(r.getId(),
                        List.of()))).collect(Collectors.toList());
    }
}
