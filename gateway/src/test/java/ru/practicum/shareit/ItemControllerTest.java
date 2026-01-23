package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemClient itemClient;

    @Test
    void create_shouldReturn400_whenRequestIdNegative() throws Exception {
        ItemCreateDto body = new ItemCreateDto("name", "desc", true, -1L);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void create_shouldForwardToClient() throws Exception {
        when(itemClient.create(eq(1L), any(ItemCreateDto.class))).thenReturn(ResponseEntity.ok().body("ok"));

        ItemCreateDto body = new ItemCreateDto("Drill", "Good", true, null);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(itemClient).create(eq(1L), any(ItemCreateDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    void update_shouldReturn400_whenNameBlank() throws Exception {
        ItemUpdateDto body = new ItemUpdateDto("   ", null, null, null);

        mvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void update_shouldForwardToClient() throws Exception {
        when(itemClient.update(eq(1L), eq(2L), any(ItemUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        ItemUpdateDto body = new ItemUpdateDto("New", null, null, null);

        mvc.perform(patch("/items/{itemId}", 2)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(itemClient).update(eq(1L), eq(2L), any(ItemUpdateDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    void getById_shouldForwardToClient() throws Exception {
        when(itemClient.getById(1L, 2L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/items/{itemId}", 2)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).getById(1L, 2L);
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    void search_shouldReturnEmptyList_whenTextBlank() throws Exception {
        mvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verifyNoInteractions(itemClient);
    }

    @Test
    void getOwnerItems_shouldReturn400_whenFromNegative() throws Exception {
        mvc.perform(get("/items")
                        .header(USER_HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getOwnerItems_shouldForwardToClient() throws Exception {
        when(itemClient.getOwnerItems(1L, 0, 10)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/items")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemClient).getOwnerItems(1L, 0, 10);
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    void search_shouldForwardToClient_whenTextNotBlank() throws Exception {
        when(itemClient.search(1L, "drill", 0, 10)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemClient).search(1L, "drill", 0, 10);
        verifyNoMoreInteractions(itemClient);
    }

    @Test
    void addComment_shouldForwardToClient() throws Exception {
        when(itemClient.addComment(eq(1L), eq(2L), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(post("/items/{itemId}/comment", 2)
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateDto("Nice"))))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(1L), eq(2L), any(CommentCreateDto.class));
        verifyNoMoreInteractions(itemClient);
    }


@Test
void create_shouldReturn400_whenUserHeaderNegative() throws Exception {
    ItemCreateDto dto = new ItemCreateDto("Drill", "Cordless drill", true, null);

    mvc.perform(post("/items")
                    .header(USER_HEADER, -1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(itemClient);
}

@Test
void addComment_shouldReturn400_whenItemIdNegative() throws Exception {
    CommentCreateDto dto = new CommentCreateDto("Nice!");

    mvc.perform(post("/items/{itemId}/comment", -1)
                    .header(USER_HEADER, 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(itemClient);
}
}
