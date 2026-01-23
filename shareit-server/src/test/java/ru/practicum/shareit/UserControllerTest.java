package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Test
    void create_shouldReturnUserDto() throws Exception {
        UserDto created = new UserDto(1L, "Igor", "igor@test.com");
        when(userService.create(any(UserDto.class))).thenReturn(created);

        UserDto body = new UserDto(null, "Igor", "igor@test.com");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Igor"))
                .andExpect(jsonPath("$.email").value("igor@test.com"));

        verify(userService).create(any(UserDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void update_shouldReturnUpdatedUserDto() throws Exception {
        long userId = 10L;
        UserDto updated = new UserDto(userId, "NewName", "new@test.com");
        when(userService.update(eq(userId), any(UserDto.class))).thenReturn(updated);

        UserDto body = new UserDto(null, "NewName", "new@test.com");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("NewName"));

        verify(userService).update(eq(userId), any(UserDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findById_shouldReturnUserDto() throws Exception {
        long userId = 5L;
        when(userService.findById(userId)).thenReturn(new UserDto(userId, "U", "u@test.com"));

        mvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("u@test.com"));

        verify(userService).findById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void findAll_shouldReturnList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(
                new UserDto(2L, "B", "b@test.com"),
                new UserDto(1L, "A", "a@test.com")
        ));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));

        verify(userService).findAll();
        verifyNoMoreInteractions(userService);
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        long userId = 7L;

        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService).delete(userId);
        verifyNoMoreInteractions(userService);
    }
}
