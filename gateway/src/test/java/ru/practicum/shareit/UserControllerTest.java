package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserClient userClient;

    @Test
    void create_shouldForwardToClient() throws Exception {
        when(userClient.create(any(UserCreateDto.class))).thenReturn(ResponseEntity.ok().body("ok"));

        UserCreateDto body = new UserCreateDto("Igor", "igor@test.com");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(userClient).create(any(UserCreateDto.class));
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void create_shouldReturn400_whenEmailInvalid() throws Exception {
        UserCreateDto body = new UserCreateDto("Igor", "bad-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void update_shouldReturn400_whenNameBlank() throws Exception {
        long userId = 1L;
        UserUpdateDto dto = new UserUpdateDto("   ", null);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void getById_shouldReturn400_whenUserIdNegative() throws Exception {
        mvc.perform(get("/users/{userId}", -1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void getById_shouldForwardToClient() throws Exception {
        when(userClient.getById(10L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/users/{userId}", 10))
                .andExpect(status().isOk());

        verify(userClient).getById(10L);
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void update_shouldForwardToClient() throws Exception {
        long userId = 10L;
        when(userClient.update(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        UserUpdateDto body = new UserUpdateDto("New", "new@test.com");

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(userClient).update(eq(userId), any(UserUpdateDto.class));
        verifyNoMoreInteractions(userClient);
    }

    @Test
    void delete_shouldForwardToClient() throws Exception {
        when(userClient.deleteById(10L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(delete("/users/{userId}", 10))
                .andExpect(status().isOk());

        verify(userClient).deleteById(10L);
        verifyNoMoreInteractions(userClient);
    }
}
