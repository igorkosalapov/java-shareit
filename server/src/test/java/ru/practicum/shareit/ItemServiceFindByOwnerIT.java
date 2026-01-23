package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceFindByOwnerIT {

    @Autowired
    ItemService itemService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void findByOwner_shouldEnrichBookingsAndComments() {
        User owner = userRepository.save(user("Owner", "owner@test.com"));
        User booker = userRepository.save(user("Booker", "booker@test.com"));

        Item itemWithBookings = itemRepository.save(item("Drill", owner, true));
        Item itemWithoutBookings = itemRepository.save(item("Saw", owner, true));

        LocalDateTime now = LocalDateTime.now();

        bookingRepository.save(booking(itemWithBookings, booker,
                now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED));

        bookingRepository.save(booking(itemWithBookings, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED));

        commentRepository.save(comment(itemWithBookings, booker, "Nice", now.minusHours(1)));

        List<ItemDto> result = itemService.findByOwner(owner.getId());
        assertEquals(2, result.size());

        Map<Long, ItemDto> byId = result.stream()
                .collect(Collectors.toMap(ItemDto::getId, Function.identity()));

        ItemDto dto1 = byId.get(itemWithBookings.getId());
        assertNotNull(dto1);
        assertNotNull(dto1.getLastBooking(), "Должна быть lastBooking для вещи владельца");
        assertNotNull(dto1.getNextBooking(), "Должна быть nextBooking для вещи владельца");
        assertFalse(dto1.getComments().isEmpty(), "Должны подтягиваться комментарии");

        ItemDto dto2 = byId.get(itemWithoutBookings.getId());
        assertNotNull(dto2);
        assertNull(dto2.getLastBooking(), "Если бронирований нет - lastBooking null");
        assertNull(dto2.getNextBooking(), "Если бронирований нет - nextBooking null");
    }

    private User user(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    private Item item(String name, User owner, boolean available) {
        Item i = new Item();
        i.setName(name);
        i.setDescription("desc");
        i.setAvailable(available);
        i.setOwner(owner);
        return i;
    }

    private Booking booking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        return b;
    }

    private Comment comment(Item item, User author, String text, LocalDateTime created) {
        Comment c = new Comment();
        c.setItem(item);
        c.setAuthor(author);
        c.setText(text);
        c.setCreated(created);
        return c;
    }
}
