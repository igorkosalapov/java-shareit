package ru.practicum.shareit.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BookingStateParser {
    private BookingStateParser() {}

    public static BookingState parseOr400(String value) {
        try {
            return BookingState.valueOf(value);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown state: " + value);
        }
    }
}
