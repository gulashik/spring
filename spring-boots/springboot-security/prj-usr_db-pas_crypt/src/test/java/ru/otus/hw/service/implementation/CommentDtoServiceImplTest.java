package ru.otus.hw.service.implementation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.dto.BookDto;
import ru.otus.hw.model.dto.CommentDto;
import ru.otus.hw.service.mapper.AuthorMapper;
import ru.otus.hw.service.mapper.BookMapper;
import ru.otus.hw.service.mapper.CommentMapper;
import ru.otus.hw.service.mapper.GenreMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.otus.hw.object.TestObjects.getDbBooks;
import static ru.otus.hw.object.TestObjects.getDbComments;

@DisplayName("Сервис на основе Jpa для работы с комментариями ")
@DataJpaTest
@Import(
    {
        CommentServiceImpl.class, CommentMapper.class,
        BookServiceImpl.class, BookMapper.class,
        AuthorMapper.class,
        GenreMapper.class
    }
)
class CommentDtoServiceImplTest {

    private final List<BookDto> dbBookDtos = getDbBooks();

    private final List<CommentDto> dbCommentDtos = getDbComments();

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentServiceImpl service;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void findById() {
        var expectedComment = dbCommentDtos.get(0);
        var actualComment = service.findById(expectedComment.getId());

        assertThat(actualComment)
            .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать список всех комментариев по книге")
    @Test
    void findAllForBook() {
        long id = dbBookDtos.get(0).getId();
        List<CommentDto> excpectedCommentDtos = dbCommentDtos
            .stream()
            .filter(comment -> comment.getBookDto().getId() == id)
            .toList();

        List<CommentDto> actualCommentDtos = service.findAllForBook(id);

        assertThat(actualCommentDtos).containsExactlyElementsOf(excpectedCommentDtos);
    }

    @DisplayName("должен сохранить комментарий")
    @Test
    void save() {
        var expectedComment = dbCommentDtos.get(0);
        expectedComment.setId(0L);
        expectedComment = service.save(expectedComment);

        entityManager.clear();
        var actualComment = service.findById(expectedComment.getId());

        assertThat(actualComment).isEqualTo(expectedComment);
    }

    @DisplayName("должен обновить комментарий")
    @Test
    void update() {
        var expectedComment = dbCommentDtos.get(0);
        expectedComment.setText("-");

        service.update(expectedComment.getId(), expectedComment.getText());
        entityManager.flush();
        var actualComment = service.findById(expectedComment.getId());


        assertThat(actualComment).isEqualTo(expectedComment);
    }

    @DisplayName("должен создать комментарий")
    @Test
    void create() {
        var expectedComment = new CommentDto(0l, "new", getDbBooks().get(0));

        expectedComment = service.create(expectedComment.getBookDto().getId(), expectedComment.getText());

        var actualComment = service.findById(expectedComment.getId());

        assertThat(actualComment).isEqualTo(expectedComment);
    }

    @DisplayName("должен удалить комментарий")
    @Test
    void deleteById() {
        var id = dbCommentDtos.get(0).getId();

        service.deleteById(id);

        entityManager.flush();

        assertThatThrownBy(() -> service.findById(id)).isExactlyInstanceOf(EntityNotFoundException.class);
    }
}