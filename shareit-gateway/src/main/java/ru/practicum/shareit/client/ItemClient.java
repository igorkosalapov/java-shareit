package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.CommentCreateDto;
import ru.practicum.shareit.dto.ItemCreateDto;
import ru.practicum.shareit.dto.ItemUpdateDto;

import java.util.HashMap;
import java.util.Map;

@Component
public class ItemClient extends BaseClient {

    public ItemClient(RestTemplate restTemplate,
                      @Value("${shareit-server.url}") String serverUrl) {
        super(restTemplate, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, ItemCreateDto dto) {
        return post("/items", userId, dto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, ItemUpdateDto dto) {
        return patch("/items/" + itemId, userId, dto);
    }

    public ResponseEntity<Object> getById(long userId, long itemId) {
        return get("/items/" + itemId, userId);
    }

    public ResponseEntity<Object> getOwnerItems(long userId, int from, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("from", from);
        params.put("size", size);
        return get("/items", userId, params);
    }

    public ResponseEntity<Object> search(long userId, String text, int from, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("text", text);
        params.put("from", from);
        params.put("size", size);
        return get("/items/search", userId, params);
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentCreateDto dto) {
        return post("/items/" + itemId + "/comment", userId, dto);
    }
}
