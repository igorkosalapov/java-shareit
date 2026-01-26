package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    JacksonTester<BookingCreateDto> json;

    @Test
    void shouldSerializeRecord() throws Exception {
        BookingCreateDto dto = new BookingCreateDto(
                LocalDateTime.of(2026, 1, 21, 10, 0, 0),
                LocalDateTime.of(2026, 1, 22, 10, 0, 0),
                99L
        );

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).hasJsonPathValue("$.start");
        assertThat(result).hasJsonPathValue("$.end");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(99);
    }
}
