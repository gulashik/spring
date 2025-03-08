package ru.otus.hw.service.implementation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.model.dto.*;
import ru.otus.hw.service.mapper.BookMapper;
import ru.otus.hw.service.mapper.GenreMapper;
import ru.otus.hw.service.mapper.AuthorMapper;
import ru.otus.hw.exception.EntityNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.otus.hw.object.TestObjects.*;


@DisplayName("Репозиторий на основе Jpa для работы с книгами ")
@DataJpaTest
@Import(
    {
        BookServiceImpl.class,
        BookMapper.class,
        GenreMapper.class,
        AuthorMapper.class
    }
)
class BookDtoServiceImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookServiceImpl service;

    private final List<AuthorDto> dbAuthorDtos = getDbAuthors();

    private final List<GenreDto> dbGenreDtos = getDbGenres();

    private final List<BookDto> dbBookDtos = getDbBooks();

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("getBooks")
    void findById(BookDto expectedBookDto) {
        var actualBook = service.findById(expectedBookDto.getId());
        assertThat(actualBook)
            .isEqualTo(expectedBookDto);
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void findAll() {
        var actualBooks = service.findAll();
        var expectedBooks = dbBookDtos;

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);
        actualBooks.forEach(System.out::println);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void insert() {
        var expectedBook = new BookDto(0l, "BookTitle_10500", dbAuthorDtos.get(0), dbGenreDtos.get(0));
        var returnedBook = service.insert(
            new BookCreateDto(
                expectedBook.getTitle(),
                expectedBook.getAuthorDto().getId(),
                expectedBook.getGenreDto().getId()
            )
        );

        assertThat(returnedBook).isNotNull()
            .matches(book -> book.getId() > 0)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .ignoringFields("id")
            .isEqualTo(expectedBook);

        assertThat(service.findById(returnedBook.getId()))
            .isEqualTo(returnedBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void update() {
        var expectedBook = new BookDto(1L, "BookTitle_10500", dbAuthorDtos.get(2), dbGenreDtos.get(2));

        assertThat(service.findById(expectedBook.getId()))
            .isNotEqualTo(expectedBook);

        var returnedBook = service.update(
            new BookUpdateDto(
                expectedBook.getId(),
                expectedBook.getTitle(),
                expectedBook.getAuthorDto().getId(),
                expectedBook.getGenreDto().getId()
            )
        );

        assertThat(returnedBook).isNotNull()
            .matches(book -> book.getId() > 0)
            .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        assertThat(service.findById(returnedBook.getId()))
            .isEqualTo(returnedBook);
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void deleteById() {
        long id = 1L;
        assertThat(service.findById(id)).isNotNull();
        entityManager.clear();

        service.deleteById(id);

        assertThatThrownBy(() -> service.findById(id)).isExactlyInstanceOf(EntityNotFoundException.class);
    }

    public static List<BookDto> getBooks() {
        return getDbBooks();
    }
}