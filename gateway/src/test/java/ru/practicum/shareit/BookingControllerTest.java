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
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingClient bookingClient;

    @Test
    void create_shouldReturn400_whenEndNotAfterStart() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingCreateDto body = new BookingCreateDto(start, end, 1L);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_shouldForwardToClient() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        when(bookingClient.create(eq(1L), any(BookingCreateDto.class)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BookingCreateDto(start, end, 99L))))
                .andExpect(status().isOk());

        verify(bookingClient).create(eq(1L), any(BookingCreateDto.class));
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    void getById_shouldReturn400_whenBookingIdNegative() throws Exception {
        mvc.perform(get("/bookings/{bookingId}", -1)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getById_shouldForwardToClient() throws Exception {
        when(bookingClient.getById(1L, 10L)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/bookings/{bookingId}", 10)
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getById(1L, 10L);
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    void approve_shouldForwardToClient() throws Exception {
        when(bookingClient.approve(1L, 10L, true)).thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(patch("/bookings/{bookingId}", 10)
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(1L, 10L, true);
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    void getBookerBookings_shouldForwardToClient() throws Exception {
        when(bookingClient.getBookerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookerBookings(eq(1L), eq("ALL"), eq(0), eq(10));
        verifyNoMoreInteractions(bookingClient);
    }

    @Test
    void getOwnerBookings_shouldForwardToClient() throws Exception {
        when(bookingClient.getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok().body("ok"));

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(eq(1L), eq("ALL"), eq(0), eq(10));
        verifyNoMoreInteractions(bookingClient);
    }


@Test
void approve_shouldReturn400_whenApprovedMissing() throws Exception {
    mvc.perform(patch("/bookings/{bookingId}", 10)
                    .header(USER_HEADER, 1L))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(bookingClient);
}

@Test
void approve_shouldReturn400_whenBookingIdNegative() throws Exception {
    mvc.perform(patch("/bookings/{bookingId}", -1)
                    .header(USER_HEADER, 1L)
                    .param("approved", "true"))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(bookingClient);
}

@Test
void create_shouldReturn400_whenStartOrEndNotFuture() throws Exception {
    BookingCreateDto dto = new BookingCreateDto(
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(1),
                    99L
            );

    mvc.perform(post("/bookings")
                    .header(USER_HEADER, 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(bookingClient);
}

@Test
void getBookerBookings_shouldReturn400_whenStateUnknown() throws Exception {
    mvc.perform(get("/bookings")
                    .header(USER_HEADER, 1L)
                    .param("state", "UNKNOWN")
                    .param("from", "0")
                    .param("size", "10"))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(bookingClient);
}
}
