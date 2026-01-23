package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIT {

    @Autowired
    UserService userService;

    @Test
    void create_shouldPersistAndReturnId() {
        UserDto created = userService.create(new UserDto(null, "Igor", "igor@test.com"));

        assertNotNull(created.getId());
        assertEquals("Igor", created.getName());
        assertEquals("igor@test.com", created.getEmail());

        UserDto loaded = userService.findById(created.getId());
        assertEquals(created.getId(), loaded.getId());
        assertEquals("Igor", loaded.getName());
    }

    @Test
    void update_shouldFailWhenEmailNotUnique() {
        UserDto u1 = userService.create(new UserDto(null, "U1", "u1@test.com"));
        UserDto u2 = userService.create(new UserDto(null, "U2", "u2@test.com"));

        UserDto patch = new UserDto();
        patch.setEmail("u1@test.com");

        assertThrows(ConflictException.class, () -> userService.update(u2.getId(), patch));
    }
}
