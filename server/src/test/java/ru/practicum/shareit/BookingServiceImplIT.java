package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIT {

    @Autowired
    BookingService bookingService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void createAndApprove_shouldPersistAndChangeStatus() {
        User owner = userRepository.save(user("Owner", "owner@test.com"));
        User booker = userRepository.save(user("Booker", "booker@test.com"));

        Item item = itemRepository.save(item("Drill", owner, true));

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto created = bookingService.create(booker.getId(), new BookingCreateDto(item.getId(), start, end));
        assertNotNull(created.getId());
        assertEquals(BookingStatus.WAITING, created.getStatus());
        assertEquals(item.getId(), created.getItem().getId());
        assertEquals(booker.getId(), created.getBooker().getId());

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void findAllByOwner_shouldReturnBookingsWhereUserIsOwnerNotBooker() {
        User owner1 = userRepository.save(user("Owner1", "owner1@test.com"));
        User owner2 = userRepository.save(user("Owner2", "owner2@test.com"));
        User booker1 = userRepository.save(user("Booker1", "booker1@test.com"));

        Item itemOwnedByOwner1 = itemRepository.save(item("I1", owner1, true));
        Item itemOwnedByOwner2 = itemRepository.save(item("I2", owner2, true));

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto b1 = bookingService.create(booker1.getId(), new BookingCreateDto(itemOwnedByOwner1.getId(), start, end));
        bookingService.approve(owner1.getId(), b1.getId(), true);

        BookingDto b2 = bookingService.create(owner1.getId(), new BookingCreateDto(itemOwnedByOwner2.getId(), start.plusDays(1), end.plusDays(1)));
        bookingService.approve(owner2.getId(), b2.getId(), true);

        List<BookingDto> owner1Bookings = bookingService.findAllByOwner(owner1.getId(), BookingState.ALL);

        assertTrue(owner1Bookings.stream().anyMatch(b -> b.getId().equals(b1.getId())),
                "Owner1 должен видеть бронирования своих вещей");
        assertTrue(owner1Bookings.stream().noneMatch(b -> b.getId().equals(b2.getId())),
                "Owner1 не должен видеть бронирования чужих вещей, даже если он booker");
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
}
