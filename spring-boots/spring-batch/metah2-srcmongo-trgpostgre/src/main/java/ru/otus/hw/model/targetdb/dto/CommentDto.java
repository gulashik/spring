package ru.otus.hw.model.targetdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id;

    private String commentText;

    private Long bookId;
}
