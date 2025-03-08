package ru.otus.hw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    private Long id;

    private String title;

    private AuthorDto authorDto;

    private GenreDto genreDto;
}
