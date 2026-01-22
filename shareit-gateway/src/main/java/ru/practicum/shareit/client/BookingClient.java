package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.BookingCreateDto;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    public BookingClient(RestTemplate restTemplate,
                         @Value("${shareit-server.url}") String serverUrl) {
        super(restTemplate, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, BookingCreateDto dto) {
        return post("/bookings", userId, dto);
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return get("/bookings/" + bookingId, userId);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, Boolean approved) {
        return patch(
                "/bookings/" + bookingId,
                userId,
                null,
                Map.of("approved", approved)
        );
    }

    public ResponseEntity<Object> getBookerBookings(long userId, String state, int from, int size) {
        return get("/bookings", userId, Map.of("state", state, "from", from, "size", size));
    }

    public ResponseEntity<Object> getOwnerBookings(long userId, String state, int from, int size) {
        return get("/bookings/owner", userId, Map.of("state", state, "from", from, "size", size));
    }
}
