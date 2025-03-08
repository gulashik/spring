package ru.otus.hw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookUpdateDto {
    private Long id;

    private String title;

    private Long authorId;

    private Long genreId;
}