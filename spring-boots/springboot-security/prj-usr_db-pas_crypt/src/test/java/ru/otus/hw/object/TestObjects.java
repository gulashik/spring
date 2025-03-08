package ru.otus.hw.object;

import ru.otus.hw.model.dto.AuthorDto;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.dto.CommentDto;
import ru.otus.hw.model.dto.GenreDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TestObjects {


    public static List<AuthorDto> getDbAuthors() {
        return LongStream.range(1, 4).boxed()
            .map(id -> new AuthorDto(id, "Author_" + id))
            .toList();
    }

    public static List<GenreDto> getDbGenres() {
        return LongStream.range(1, 4).boxed()
            .map(id -> new GenreDto(id, "Genre_" + id))
            .toList();
    }

    public static List<BookDto> getDbBooks(List<AuthorDto> dbAuthorDtos, List<GenreDto> dbGenreDtos) {
        return LongStream.range(1, 4).boxed()
            .map(id -> new BookDto(id, "BookTitle_" + id, dbAuthorDtos.get(id.intValue() - 1), dbGenreDtos.get(id.intValue() - 1)))
            .toList();
    }

    public static List<BookDto> getDbBooks() {
        return getDbBooks( getDbAuthors(), getDbGenres());
    }

    public static List<CommentDto> getDbComments() {
        Map<Long, List<BookDto>> mapBook = getDbBooks()
            .stream()
            .collect(Collectors.groupingBy(BookDto::getId));
        var bookOne = mapBook.get(1L).get(0);
        var bookTwo = mapBook.get(2L).get(0);

        return List.of(
            new CommentDto(1L, "Comment_1_book_1", bookOne),
            new CommentDto(2L, "Comment_2_book_1", bookOne),
            new CommentDto(3L, "Comment_3_book_1", bookOne),

            new CommentDto(4L, "Comment_1_book_2", bookTwo),
            new CommentDto(5L, "Comment_2_book_2", bookTwo)
        );
    }
}
