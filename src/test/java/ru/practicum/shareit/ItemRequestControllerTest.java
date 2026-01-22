package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Test
    void postRequests_shouldReturnOk_andBody() throws Exception {
        long userId = 1L;

        ItemRequestResponseDto response = new ItemRequestResponseDto(
                10L,
                "Need a drill",
                LocalDateTime.of(2026, 1, 21, 10, 0, 0),
                List.of(new ItemForRequestDto(99L, "Drill", 2L))
        );

        when(itemRequestService.create(eq(userId), ArgumentMatchers.any(ItemRequestCreateDto.class)))
                .thenReturn(response);

        ItemRequestCreateDto body = new ItemRequestCreateDto("Need a drill");

        mvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(99))
                .andExpect(jsonPath("$.items[0].name").value("Drill"))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));

        verify(itemRequestService, times(1)).create(eq(userId), any(ItemRequestCreateDto.class));
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void postRequests_shouldReturn400_whenDescriptionBlank() throws Exception {
        long userId = 1L;

        ItemRequestCreateDto body = new ItemRequestCreateDto("   ");

        mvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestService);
    }

    @Test
    void getRequests_shouldReturnList() throws Exception {
        long userId = 1L;

        List<ItemRequestResponseDto> response = List.of(
                new ItemRequestResponseDto(2L, "Second", LocalDateTime.now(), List.of()),
                new ItemRequestResponseDto(1L, "First", LocalDateTime.now().minusHours(1), List.of())
        );

        when(itemRequestService.getOwn(userId)).thenReturn(response);

        mvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));

        verify(itemRequestService, times(1)).getOwn(userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getRequestsAll_shouldReturnList() throws Exception {
        long userId = 1L;

        when(itemRequestService.getAllOthers(userId)).thenReturn(List.of(
                new ItemRequestResponseDto(100L, "Other request", LocalDateTime.now(), List.of())
        ));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(100));

        verify(itemRequestService, times(1)).getAllOthers(userId);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getRequestById_shouldReturnDto() throws Exception {
        long userId = 1L;
        long requestId = 10L;

        ItemRequestResponseDto response = new ItemRequestResponseDto(
                requestId,
                "Need a saw",
                LocalDateTime.now(),
                List.of(new ItemForRequestDto(5L, "Saw", 2L))
        );

        when(itemRequestService.getById(userId, requestId)).thenReturn(response);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].ownerId").value(2));

        verify(itemRequestService, times(1)).getById(userId, requestId);
        verifyNoMoreInteractions(itemRequestService);
    }
}
