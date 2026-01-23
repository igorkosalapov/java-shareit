package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceCreateWithRequestIT {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Test
    void createItem_shouldSaveRequestLink_whenRequestIdProvided() {
        User requestor = userRepository.save(user("Req", "req@test.com"));
        User owner = userRepository.save(user("Owner", "owner@test.com"));

        ItemRequest req = itemRequestRepository.save(request("Need drill", requestor));

        ItemDto create = new ItemDto();
        create.setName("Drill");
        create.setDescription("Good drill");
        create.setAvailable(true);
        create.setRequestId(req.getId());

        ItemDto created = itemService.create(owner.getId(), create);

        Item saved = itemRepository.findById(created.getId()).orElseThrow();
        assertNotNull(saved.getRequest(), "У вещи должен быть проставлен request");
        assertEquals(req.getId(), saved.getRequest().getId(), "requestId должен совпасть");
    }

    @Test
    void createItem_shouldThrow404_whenRequestIdNotFound() {
        User owner = userRepository.save(user("Owner2", "owner2@test.com"));

        ItemDto create = new ItemDto();
        create.setName("Hammer");
        create.setDescription("Nice");
        create.setAvailable(true);
        create.setRequestId(99999L);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), create));
    }

    private User user(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private ItemRequest request(String description, User requestor) {
        ItemRequest r = new ItemRequest();
        r.setDescription(description);
        r.setRequestor(requestor);
        r.setCreated(LocalDateTime.now());
        return r;
    }
}
