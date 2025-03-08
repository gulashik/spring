package ru.otus.hw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookCreateDto {
    private String title;

    private Long authorId;

    private Long genreId;
}