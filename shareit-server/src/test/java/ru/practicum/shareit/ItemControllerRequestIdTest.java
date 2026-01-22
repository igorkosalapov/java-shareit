package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerRequestIdTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ItemService itemService;

    @Test
    void postItems_shouldPassRequestIdToService() throws Exception {
        long userId = 1L;

        ItemDto returned = new ItemDto();
        returned.setId(10L);
        returned.setName("Drill");
        returned.setDescription("Good");
        returned.setAvailable(true);
        returned.setRequestId(55L);

        when(itemService.create(eq(userId), any(ItemDto.class))).thenReturn(returned);

        ItemDto body = new ItemDto();
        body.setName("Drill");
        body.setDescription("Good");
        body.setAvailable(true);
        body.setRequestId(55L);

        mvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.requestId").value(55));

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).create(eq(userId), captor.capture());
        assertEquals(55L, captor.getValue().getRequestId());
        verifyNoMoreInteractions(itemService);
    }
}
