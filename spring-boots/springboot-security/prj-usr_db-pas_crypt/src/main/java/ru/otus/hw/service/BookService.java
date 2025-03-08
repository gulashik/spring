package ru.otus.hw.service;

import ru.otus.hw.model.dto.BookCreateDto;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.dto.BookUpdateDto;

import java.util.List;

public interface BookService {
    BookDto findById(long id);

    List<BookDto> findAll();

    BookDto insert(BookCreateDto bookCreateDto);

    BookDto update(BookUpdateDto bookUpdateDto);

    void deleteById(long id);
}
