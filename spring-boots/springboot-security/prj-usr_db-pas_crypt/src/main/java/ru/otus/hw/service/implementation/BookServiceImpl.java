package ru.otus.hw.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.dto.BookCreateDto;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.dto.BookUpdateDto;
import ru.otus.hw.model.entity.Author;
import ru.otus.hw.model.entity.Book;
import ru.otus.hw.model.entity.Genre;
import ru.otus.hw.repository.AuthorRepository;
import ru.otus.hw.repository.BookRepository;
import ru.otus.hw.repository.GenreRepository;
import ru.otus.hw.service.BookService;
import ru.otus.hw.service.mapper.AuthorMapper;
import ru.otus.hw.service.mapper.BookMapper;
import ru.otus.hw.service.mapper.GenreMapper;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final AuthorMapper authorMapper;

    private final GenreMapper genreMapper;

    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        return bookRepository
            .findById(id)
            .map(bookMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException("No book"));
    }

    @Transactional
    @Override
    public List<BookDto> findAll() {
        return bookRepository
            .findAll()
            .stream()
            .map(bookMapper::toDto)
            .toList();
    }

    @Transactional
    @Override
    public BookDto insert(BookCreateDto bookCreateDto) {
        return save(0, bookCreateDto.getTitle(), bookCreateDto.getAuthorId(), bookCreateDto.getGenreId());
    }

    @Transactional
    @Override
    public BookDto update(BookUpdateDto bookUpdateDto) {
        long id = bookUpdateDto.getId();
        String title = bookUpdateDto.getTitle();
        long authorId = bookUpdateDto.getAuthorId();
        long genreId = bookUpdateDto.getGenreId();

        Book book = bookRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Book with id " + id + " not found"));

        Author author = authorRepository
            .findById(authorId)
            .orElseThrow(() -> new EntityNotFoundException("Author with id " + authorId + " not found"));

        Genre genre = genreRepository
            .findById(genreId)
            .orElseThrow(() -> new EntityNotFoundException("Genre with id " + genreId + " not found"));

        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);

        return bookMapper.toDto(book);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }


    private BookDto save(long id, String title, long authorId, long genreId) {
        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %d not found".formatted(genreId)));
        var book = new BookDto(id, title, authorMapper.toDto(author), genreMapper.toDto(genre));
        var savedBook = bookRepository.save(bookMapper.toEntity(book));
        return bookMapper.toDto(savedBook);
    }
}
