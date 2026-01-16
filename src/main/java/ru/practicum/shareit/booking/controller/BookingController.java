package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.common.Headers.USER_ID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID) Long userId, @Valid @RequestBody BookingCreateDto request) {
        validateBookingDates(request.getStart(), request.getEnd());
        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader(USER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved
    ) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader(USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findAllByBooker(
            @RequestHeader(USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        return bookingService.findAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(
            @RequestHeader(USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state
    ) {
        return bookingService.findAllByOwner(userId, state);
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("Дата начала и окончания бронирования обязательны");
        }
        if (!start.isBefore(end)) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!end.isAfter(now) || !start.isAfter(now)) {
            throw new BadRequestException("Дата бронирования должна быть в будущем");
        }
    }
}
