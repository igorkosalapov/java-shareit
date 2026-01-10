package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userService.findByIdOrThrow(userId);

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
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

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)
                .stream()
                .map(commentMapper::toDto)
                .toList());

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            enrichBookingsForOwner(dto, itemId, now);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findByOwner(Long ownerId) {
        List<Item> items = itemRepository.findAllByOwner_Id(ownerId);
        List<ItemDto> dtos = itemMapper.toDtoList(items);

        if (dtos.isEmpty()) {
            return dtos;
        }

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<CommentDto>> commentsByItemId = commentRepository
                .findAllByItemIdsWithItemAndAuthorOrderByCreatedDesc(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();
        Map<Long, Booking> lastByItemId = new HashMap<>();
        for (Booking b : bookingRepository.findLastBookingsForItems(itemIds, BookingStatus.APPROVED, now)) {
            lastByItemId.putIfAbsent(b.getItem().getId(), b);
        }

        Map<Long, Booking> nextByItemId = new HashMap<>();
        for (Booking b : bookingRepository.findNextBookingsForItems(itemIds, BookingStatus.APPROVED, now)) {
            nextByItemId.putIfAbsent(b.getItem().getId(), b);
        }

        for (ItemDto dto : dtos) {
            Long itemId = dto.getId();
            dto.setComments(commentsByItemId.getOrDefault(itemId, List.of()));
            setLastNextFromMaps(dto, lastByItemId, nextByItemId);
        }

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemMapper.toDtoList(itemRepository.searchAvailable(text));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        User author = userService.findByIdOrThrow(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        Booking booking = bookingRepository
                .findFirstByItem_IdAndBooker_IdAndStatusOrderByEndDesc(itemId, userId, BookingStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException("Комментарий можно оставить только после завершённого бронирования"));

        if (booking.getEnd().isAfter(now)) { // end > now => ещё не закончилось
            throw new BadRequestException("Комментарий можно оставить только после завершённого бронирования");
        }

        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        return commentMapper.toDto(commentRepository.save(comment));
    }

    private void enrichBookingsForOwner(ItemDto dto, Long itemId, LocalDateTime now) {
        dto.setLastBooking(
                bookingRepository.findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(itemId, BookingStatus.APPROVED, now)
                        .map(bookingMapper::toShortDto)
                        .orElse(null)
        );

        dto.setNextBooking(
                bookingRepository.findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                        .map(bookingMapper::toShortDto)
                        .orElse(null)
        );
    }

    private void setLastNextFromMaps(ItemDto dto, Map<Long, Booking> lastByItemId, Map<Long, Booking> nextByItemId) {
        Long itemId = dto.getId();
        dto.setLastBooking(bookingMapper.toShortDto(lastByItemId.get(itemId)));
        dto.setNextBooking(bookingMapper.toShortDto(nextByItemId.get(itemId)));
    }

}
