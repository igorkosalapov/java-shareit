package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserUpdateDto;

@Component
public class UserClient extends BaseClient {

    public UserClient(RestTemplate restTemplate,
                      @Value("${shareit-server.url}") String serverUrl) {
        super(restTemplate, serverUrl);
    }

    public ResponseEntity<Object> create(UserCreateDto dto) {
        // users в оригинальном API без X-Sharer-User-Id, но BaseClient его всегда добавляет.
        // Это обычно не мешает: server просто проигнорирует хедер.
        return post("/users", 0L, dto);
    }

    public ResponseEntity<Object> update(long userId, UserUpdateDto dto) {
        return patch("/users/" + userId, 0L, dto);
    }

    public ResponseEntity<Object> getById(long userId) {
        return get("/users/" + userId, 0L);
    }

    public ResponseEntity<Object> deleteById(long userId) {
        return delete("/users/" + userId, 0L);
    }
}
