package ru.otus.hw.migration.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.otus.hw.model.targetdb.dto.AuthorDto;
import ru.otus.hw.model.targetdb.dto.BookDto;
import ru.otus.hw.model.targetdb.dto.CommentDto;
import ru.otus.hw.model.targetdb.dto.GenreDto;
import ru.otus.hw.repositories.jpa.JpaAuthorRepository;
import ru.otus.hw.repositories.jpa.JpaBookRepository;
import ru.otus.hw.repositories.jpa.JpaCommentRepository;
import ru.otus.hw.repositories.jpa.JpaGenreRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.otus.hw.migration.job.Job.MIGRATE_JOB_NAME;


@SpringBootTest
@SpringBatchTest// todo нужно указать
@Testcontainers
class JobTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils; // todo для тестов

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils; // todo для тестов

    @Autowired
    private JpaAuthorRepository jpaAuthorRepository;

    @Autowired
    private JpaBookRepository jpaBookRepository;

    @Autowired
    private JpaCommentRepository jpaCommentRepository;

    @Autowired
    private JpaGenreRepository jpaGenreRepository;



    @BeforeEach
    void clearMetaData() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.target-datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.target-datasource.username", postgresContainer::getUsername);
        registry.add("spring.target-datasource.password", postgresContainer::getPassword);
    }


    @Test
    void testJob() throws Exception {
        // todo Job уже есть один
        final Job job = jobLauncherTestUtils.getJob();

        assertThat(job).isNotNull()
            .extracting(Job::getName)
            .isEqualTo(MIGRATE_JOB_NAME);

        JobParameters parameters = new JobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);

        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Author
        final List<AuthorDto> authors = jpaAuthorRepository.findAll().stream()
            .map(
                author -> {
                    final AuthorDto authorDto = new AuthorDto();
                    authorDto.setId(author.getId());
                    authorDto.setFullName(author.getFullName());
                    return authorDto;
                }
            )
            .collect(Collectors.toList());

        assertThat(authors)
            .isNotEmpty()
            .isEqualTo(getExpectedAuthors());

        // Genre
        final List<GenreDto> genres = jpaGenreRepository.findAll().stream()
            .map(
                genre -> {
                    final GenreDto genreDto = new GenreDto();
                    genreDto.setId(genre.getId());
                    genreDto.setName(genre.getName());
                    return genreDto;
                }
            )
            .collect(Collectors.toList());

        assertThat(genres)
            .isNotEmpty()
            .isEqualTo(getExpectedGenres());

        // Book
        final List<BookDto> books = jpaBookRepository.findAll().stream()
            .map(
                book -> {
                    final BookDto bookDto = new BookDto();
                    bookDto.setId(book.getId());
                    bookDto.setTitle(book.getTitle());
                    bookDto.setAuthorId(book.getAuthor().getId());
                    bookDto.setGenreId(book.getGenre().getId());
                    return bookDto;
                }
            )
            .collect(Collectors.toList());
        assertThat(books).containsExactlyElementsOf(getExpectedBooks());

        // Comment
        final List<CommentDto> comments = jpaCommentRepository.findAll().stream()
            .map(
                comment -> new CommentDto(comment.getId(), comment.getCommentText(), comment.getBook().getId())
            )
            .collect(Collectors.toList());

        assertThat(comments)
            .isNotEmpty()
            .isEqualTo(getExpectedComments());
    }

    private List<AuthorDto> getExpectedAuthors() {
        return List.of(
            new AuthorDto(1L, "Author_1"),
            new AuthorDto(2L, "Author_2"),
            new AuthorDto(3L, "Author_3")
        );
    }

    private List<GenreDto> getExpectedGenres() {
        return List.of(
            new GenreDto(1L, "Genre_1"),
            new GenreDto(2L, "Genre_2"),
            new GenreDto(3L, "Genre_3")
        );
    }

    private List<BookDto> getExpectedBooks() {
        return List.of(
            new BookDto(1L, "BookTitle_1", 3L, 2L),
            new BookDto(2L, "BookTitle_2", 1L, 3L),
            new BookDto(3L, "BookTitle_3", 2L, 1L)
        );
    }

    private List<CommentDto> getExpectedComments() {
        return List.of(
            new CommentDto(1L, "Comment_1", 3L),
            new CommentDto(2L, "Comment_2", 2L),
            new CommentDto(3L, "Comment_3", 1L),
            new CommentDto(4L, "Comment_4", 1L),
            new CommentDto(5L, "Comment_5", 2L),
            new CommentDto(6L, "Comment_6", 1L)
        );
    }
}
