package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingCreateDto request);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findAllByBooker(Long userId, BookingState state);

    List<BookingDto> findAllByOwner(Long ownerId, BookingState state);
}
