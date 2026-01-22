package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIT {

    @Autowired
    ItemRequestService itemRequestService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void create_shouldSaveRequest_andReturnDto() {
        User u = userRepository.save(makeUser("Igor", "igor@test.com"));

        ItemRequestResponseDto dto = itemRequestService.create(u.getId(),
                new ItemRequestCreateDto("Need a drill"));

        assertNotNull(dto.getId());
        assertEquals("Need a drill", dto.getDescription());
        assertNotNull(dto.getCreated());
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());

        ItemRequest saved = itemRequestRepository.findById(dto.getId()).orElseThrow();
        assertEquals("Need a drill", saved.getDescription());
        assertEquals(u.getId(), saved.getRequestor().getId());
    }

    @Test
    void getOwn_shouldReturnDescByCreated_andAttachItems() {
        User requestor = userRepository.save(makeUser("Req", "req@test.com"));
        User owner = userRepository.save(makeUser("Owner", "owner@test.com"));

        ItemRequest oldReq = itemRequestRepository.save(makeRequest("Old request", requestor,
                LocalDateTime.now().minusDays(1)));
        ItemRequest newReq = itemRequestRepository.save(makeRequest("New request", requestor,
                LocalDateTime.now()));

        Item item = makeItem("Drill", "Good drill", true, owner, newReq);
        item = itemRepository.save(item);

        List<ItemRequestResponseDto> own = itemRequestService.getOwn(requestor.getId());

        assertEquals(2, own.size(), "Должно вернуться 2 запроса пользователя");
        assertEquals(newReq.getId(), own.get(0).getId(), "Первым должен быть более новый запрос");
        assertEquals(oldReq.getId(), own.get(1).getId(), "Вторым должен быть более старый запрос");

        ItemRequestResponseDto dtoNew = own.get(0);
        assertEquals(1, dtoNew.getItems().size(), "У нового запроса должен быть 1 ответ (item)");
        assertEquals(item.getId(), dtoNew.getItems().get(0).getId());
        assertEquals("Drill", dtoNew.getItems().get(0).getName());
        assertEquals(owner.getId(), dtoNew.getItems().get(0).getOwnerId());

        ItemRequestResponseDto dtoOld = own.get(1);
        assertTrue(dtoOld.getItems().isEmpty(), "У старого запроса нет ответов");
    }

    @Test
    void getAllOthers_shouldExcludeOwnRequests_andReturnOthers() {
        User u1 = userRepository.save(makeUser("U1", "u1@test.com"));
        User u2 = userRepository.save(makeUser("U2", "u2@test.com"));

        ItemRequest req1 = itemRequestRepository.save(makeRequest("U1 request", u1,
                LocalDateTime.now().minusHours(2)));
        ItemRequest req2 = itemRequestRepository.save(makeRequest("U2 request", u2,
                LocalDateTime.now().minusHours(1)));

        List<ItemRequestResponseDto> othersForU1 = itemRequestService.getAllOthers(u1.getId());

        assertEquals(1, othersForU1.size(), "U1 должен видеть только чужие запросы");
        assertEquals(req2.getId(), othersForU1.get(0).getId());
        assertNotEquals(req1.getId(), othersForU1.get(0).getId());
    }

    @Test
    void getById_shouldReturnRequestWithItsItems() {
        User requestor = userRepository.save(makeUser("Req", "req2@test.com"));
        User owner = userRepository.save(makeUser("Owner", "owner2@test.com"));
        User anyViewer = userRepository.save(makeUser("Viewer", "viewer@test.com"));

        ItemRequest req = itemRequestRepository.save(makeRequest("Need two items", requestor,
                LocalDateTime.now()));

        Item item1 = itemRepository.save(makeItem("Item1", "d1", true, owner, req));
        Item item2 = itemRepository.save(makeItem("Item2", "d2", true, owner, req));

        ItemRequestResponseDto dto = itemRequestService.getById(anyViewer.getId(), req.getId());

        assertEquals(req.getId(), dto.getId());
        assertEquals("Need two items", dto.getDescription());
        assertNotNull(dto.getCreated());

        assertEquals(2, dto.getItems().size(), "Должно вернуться 2 item-ответа");
        assertTrue(dto.getItems().stream().anyMatch(i -> i.getId().equals(item1.getId()) &&
                i.getOwnerId().equals(owner.getId())));
        assertTrue(dto.getItems().stream().anyMatch(i -> i.getId().equals(item2.getId()) &&
                i.getOwnerId().equals(owner.getId())));
    }

    private User makeUser(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private ItemRequest makeRequest(String description, User requestor, LocalDateTime created) {
        ItemRequest r = new ItemRequest();
        r.setDescription(description);
        r.setRequestor(requestor);
        r.setCreated(created);
        return r;
    }

    private Item makeItem(String name, String description, boolean available, User owner, ItemRequest request) {
        Item i = new Item();
        i.setName(name);
        i.setDescription(description);
        i.setAvailable(available);
        i.setOwner(owner);
        i.setRequest(request);
        return i;
    }
}
