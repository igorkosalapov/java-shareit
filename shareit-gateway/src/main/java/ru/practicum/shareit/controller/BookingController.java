package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.BookingClient;
import ru.practicum.shareit.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/bookings")
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                         @Valid @RequestBody BookingCreateDto dto) {
        validateDates(dto.start(), dto.end());
        return bookingClient.create(userId, dto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                          @PathVariable @Positive long bookingId) {
        return bookingClient.getById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader(BaseClient.USER_HEADER) long userId,
            @PathVariable long bookingId,
            @RequestParam(required = false) Boolean approved
    ) {
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookerBookings(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0") @Min(0) int from,
                                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        return bookingClient.getBookerBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(BaseClient.USER_HEADER) long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return;
        }
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(BAD_REQUEST, "end must be after start");
        }
    }
}
