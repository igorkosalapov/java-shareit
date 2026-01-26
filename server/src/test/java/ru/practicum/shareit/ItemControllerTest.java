package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemService itemService;

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        long userId = 1L;
        long itemId = 2L;

        ItemDto updated = new ItemDto();
        updated.setId(itemId);
        updated.setName("New");
        updated.setDescription("New desc");
        updated.setAvailable(true);

        when(itemService.update(eq(userId), eq(itemId), any(ItemDto.class))).thenReturn(updated);

        ItemDto body = new ItemDto();
        body.setName("New");

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New"));

        verify(itemService).update(eq(userId), eq(itemId), any(ItemDto.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void findById_shouldReturnItem() throws Exception {
        long userId = 1L;
        long itemId = 2L;

        ItemDto dto = new ItemDto();
        dto.setId(itemId);
        dto.setName("Drill");
        dto.setDescription("Good");
        dto.setAvailable(true);

        when(itemService.findById(userId, itemId)).thenReturn(dto);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).findById(userId, itemId);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        long userId = 1L;
        long itemId = 2L;

        CommentDto response = new CommentDto(10L, "Nice", "Igor", LocalDateTime.now());
        when(itemService.addComment(eq(userId), eq(itemId), any(CommentCreateDto.class))).thenReturn(response);

        CommentCreateDto body = new CommentCreateDto("Nice");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("Nice"));

        verify(itemService).addComment(eq(userId), eq(itemId), any(CommentCreateDto.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void findByOwner_shouldReturnList() throws Exception {
        long ownerId = 1L;
        when(itemService.findByOwner(ownerId)).thenReturn(List.of(new ItemDto(), new ItemDto()));

        mvc.perform(get("/items")
                        .header(USER_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(itemService).findByOwner(ownerId);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void searchAvailable_shouldReturnList() throws Exception {
        long userId = 1L;
        when(itemService.searchAvailable("drill")).thenReturn(List.of(new ItemDto()));

        mvc.perform(get("/items/search")
                        .header(USER_HEADER, userId)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(itemService).searchAvailable("drill");
        verifyNoMoreInteractions(itemService);
    }
}
