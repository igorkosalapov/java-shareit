package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.booking.client.BookingClient;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/bookings")
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    private static final Set<String> ALLOWED =
            Set.of("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED");

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                         @Valid @RequestBody BookingCreateDto dto) {
        validateDates(dto.start(), dto.end());
        return bookingClient.create(userId, dto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                          @PathVariable @Positive long bookingId) {
        return bookingClient.getById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
            @PathVariable @Positive long bookingId,
            @RequestParam @NotNull Boolean approved
    ) {
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookerBookings(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                                    @RequestParam(defaultValue = "ALL") String state,
                                                    @RequestParam(defaultValue = "0") @Min(0) int from,
                                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        validateState(state);
        return bookingClient.getBookerBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(BaseClient.USER_HEADER) @Positive long userId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") @Min(0) int from,
                                                   @RequestParam(defaultValue = "10") @Min(1) int size) {
        validateState(state);
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {

        if (start == null || end == null) {
            throw new ResponseStatusException(BAD_REQUEST, "start/end must not be null");
        }
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(BAD_REQUEST, "end must be after start");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!start.isAfter(now) || !end.isAfter(now)) {
            throw new ResponseStatusException(BAD_REQUEST, "booking dates must be in the future");
        }
    }

    private void validateState(String state) {
        if (state == null) return;
        if (!ALLOWED.contains(state)) {
            throw new ResponseStatusException(BAD_REQUEST, "Unknown state: " + state);
        }
    }
}
