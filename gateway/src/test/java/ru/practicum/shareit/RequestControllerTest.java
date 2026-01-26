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
import ru.practicum.shareit.request.client.RequestClient;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
@Import(ErrorHandler.class)
class RequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RequestClient requestClient;

    @Test
    void create_shouldReturn400_whenDescriptionBlank() throws Exception {
        ItemRequestCreateDto body = new ItemRequestCreateDto("   ");

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void create_shouldForwardToClient() throws Exception {
        when(requestClient.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestCreateDto("Need drill"))))
                .andExpect(status().isOk());

        verify(requestClient).create(eq(1L), any(ItemRequestCreateDto.class));
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    void getById_shouldReturn400_whenRequestIdNegative() throws Exception {
        mvc.perform(get("/requests/{requestId}", -1)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(requestClient);
    }

    @Test
    void getOwn_shouldForwardToClient() throws Exception {
        when(requestClient.getOwn(1L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(requestClient).getOwn(1L);
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    void getAll_shouldForwardToClient() throws Exception {
        when(requestClient.getAll(1L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(requestClient).getAll(1L);
        verifyNoMoreInteractions(requestClient);
    }

    @Test
    void getById_shouldForwardToClient() throws Exception {
        when(requestClient.getById(1L, 10L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/requests/{requestId}", 10)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(requestClient).getById(1L, 10L);
        verifyNoMoreInteractions(requestClient);
    }


@Test
void create_shouldReturn400_whenUserHeaderNegative() throws Exception {
    ItemRequestCreateDto dto = new ItemRequestCreateDto("Need a ladder");

    mvc.perform(post("/requests")
                    .header(USER_HEADER, -1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(requestClient);
}
}
