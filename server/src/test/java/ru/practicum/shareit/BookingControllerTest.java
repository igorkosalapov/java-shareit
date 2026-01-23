package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingService bookingService;

    @Test
    void create_shouldReturnBookingDto() throws Exception {
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto response = new BookingDto(
                10L,
                start,
                end,
                BookingStatus.WAITING,
                new BookingItemDto(2L, "Drill"),
                new BookingUserDto(userId)
        );

        when(bookingService.create(eq(userId), any(BookingCreateDto.class))).thenReturn(response);

        BookingCreateDto body = new BookingCreateDto(2L, start, end);

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(2))
                .andExpect(jsonPath("$.booker.id").value(1));

        verify(bookingService).create(eq(userId), any(BookingCreateDto.class));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findAllByBooker_shouldReturnList() throws Exception {
        long userId = 1L;
        when(bookingService.findAllByBooker(eq(userId), eq(BookingState.ALL)))
                .thenReturn(List.of(new BookingDto(), new BookingDto()));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bookingService).findAllByBooker(eq(userId), eq(BookingState.ALL));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findAllByBooker_shouldReturn400_whenStateUnknown() throws Exception {
        long userId = 1L;

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void approve_shouldCallService() throws Exception {
        long ownerId = 2L;
        long bookingId = 10L;

        when(bookingService.approve(ownerId, bookingId, true)).thenReturn(new BookingDto());

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingService).approve(ownerId, bookingId, true);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findById_shouldCallService() throws Exception {
        long userId = 1L;
        long bookingId = 10L;
        when(bookingService.findById(userId, bookingId)).thenReturn(new BookingDto());

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());

        verify(bookingService).findById(userId, bookingId);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findAllByOwner_shouldCallService() throws Exception {
        long ownerId = 1L;
        when(bookingService.findAllByOwner(eq(ownerId), eq(BookingState.ALL)))
                .thenReturn(List.of(new BookingDto()));

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, ownerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookingService).findAllByOwner(eq(ownerId), eq(BookingState.ALL));
        verifyNoMoreInteractions(bookingService);
    }
}
