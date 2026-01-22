package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.ItemRequestCreateDto;


@Component
public class RequestClient extends BaseClient {

    public RequestClient(RestTemplate restTemplate,
                         @Value("${shareit-server.url}") String serverUrl) {
        super(restTemplate, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, ItemRequestCreateDto dto) {
        return post("/requests", userId, dto);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("/requests", userId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("/requests/all", userId);
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return get("/requests/" + requestId, userId);
    }
}
