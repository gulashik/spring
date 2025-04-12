package ru.otus.hw.migration.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.migration.item.processor.BookItemProcessorImpl;
import ru.otus.hw.model.sourcedb.dto.BookDto;
import ru.otus.hw.model.sourcedb.entity.Book;
import ru.otus.hw.model.sourcedb.entity.Comment;

import javax.sql.DataSource;

import java.util.List;

import static ru.otus.hw.migration.job.Job.CHUNK_SIZE;

@Slf4j
@Component
public class BookStep {

    private final DataSource postgresDataSource;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public BookStep(
        @Qualifier("postgresDataSource") DataSource postgresDataSource,
        JobRepository jobRepository,
        PlatformTransactionManager platformTransactionManager
    ) {
        this.postgresDataSource = postgresDataSource;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public TaskletStep createTempTableBook() {
        return new StepBuilder("createTempTableBook", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "CREATE TABLE IF NOT EXISTS temp_table_book " +
                            "(id_src VARCHAR(255) NOT NULL UNIQUE, id_trg BIGINT NOT NULL UNIQUE)"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public Step migrationBookStep(
        final RepositoryItemReader<Book> reader,
        final CompositeItemWriter<BookDto> writer,
        final BookItemProcessorImpl processor
    ) {
        return new StepBuilder("migrationBookStep", jobRepository)
            .<Book, BookDto>chunk(CHUNK_SIZE, platformTransactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .allowStartIfComplete(true)

            // todo можно перехватить момент выполнения
            .listener(
                new ItemReadListener<>() {
                    public void beforeRead() {
                        log.info("Начало чтения");
                    }

                    public void afterRead(Book o) {
                        log.info("Конец чтения");
                    }

                    public void onReadError(Exception e) {
                        log.info("Ошибка чтения");
                    }
                }
            )
            .listener(new ItemWriteListener<Book>() {
                public void beforeWrite(List<Book> list) {
                    log.info("Начало записи");
                }

                public void afterWrite(List<Book> list) {
                    log.info("Конец записи");
                }

                public void onWriteError(Exception e, List<Book> list) {
                    log.info("Ошибка записи");
                }
            })
            .listener(new ItemProcessListener<Book,Book>() {
                public void beforeProcess(Book o) {
                    log.info("Начало обработки");
                }

                public void afterProcess(Book o, Book o2) {
                    log.info("Конец обработки");
                }

                public void onProcessError(Book o, Exception e) {
                    log.info("Ошибка обработки");
                }
            })
            .listener(new ChunkListener() {
                public void beforeChunk(ChunkContext chunkContext) {
                    log.info("Начало пачки");
                }

                public void afterChunk(ChunkContext chunkContext) {
                    log.info("Конец пачки");
                }

                public void afterChunkError(ChunkContext chunkContext) {
                    log.info("Ошибка пачки");
                }
            })
//          .taskExecutor(new SimpleAsyncTaskExecutor())
            .build();
    }

    @Bean
    public TaskletStep createTempSeqBook() {
        return new StepBuilder("createTempSeqBook", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute("CREATE SEQUENCE IF NOT EXISTS seq_book_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempTableBook() {
        return new StepBuilder("dropTempTableBook", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP TABLE temp_table_book");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempSeqBook() {
        return new StepBuilder("dropTempSeqBook", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP SEQUENCE IF EXISTS seq_book_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }
}
