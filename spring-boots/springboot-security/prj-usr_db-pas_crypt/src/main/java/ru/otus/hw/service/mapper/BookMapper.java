package ru.otus.hw.service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.entity.Book;

@RequiredArgsConstructor
@Component
public class BookMapper {
    private final AuthorMapper authorMapper;

    private final GenreMapper genreMapper;

    public BookDto toDto(Book entity) {
        var author = authorMapper.toDto(entity.getAuthor());
        var genre = genreMapper.toDto(entity.getGenre());

        return new BookDto(
            entity.getId(),
            entity.getTitle(),
            author,
            genre
        );
    }

    public Book toEntity(BookDto dto) {
        var authorEntity = authorMapper.toEntity(dto.getAuthorDto());
        var genreEntity = genreMapper.toEntity(dto.getGenreDto());

        return new Book(
            dto.getId(),
            dto.getTitle(),
            authorEntity,
            genreEntity
        );
    }
}
