package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime created;
}
