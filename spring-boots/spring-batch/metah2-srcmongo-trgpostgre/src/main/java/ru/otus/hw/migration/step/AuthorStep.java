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
import ru.otus.hw.migration.item.processor.AuthorItemProcessorImpl;
import ru.otus.hw.model.sourcedb.dto.AuthorDto;
import ru.otus.hw.model.sourcedb.entity.Author;
import ru.otus.hw.model.sourcedb.entity.Comment;

import javax.sql.DataSource;

import java.util.List;

import static ru.otus.hw.migration.job.Job.CHUNK_SIZE;

@Slf4j
@Component
public class AuthorStep {

    private final DataSource postgresDataSource;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public AuthorStep(
        @Qualifier("postgresDataSource") DataSource postgresDataSource,
        JobRepository jobRepository,
        PlatformTransactionManager platformTransactionManager
    ) {
        this.postgresDataSource = postgresDataSource;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public TaskletStep createTempTableAuthor() {
        return new StepBuilder("createTempTableAuthor", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "CREATE TABLE IF NOT EXISTS temp_table_author " +
                            "(id_src VARCHAR(255) NOT NULL UNIQUE, id_trg BIGINT NOT NULL UNIQUE)"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempTableAuthor() {
        return new StepBuilder("dropTempTableAuthor", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP TABLE temp_table_author");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep createTempSeqAuthor() {
        return new StepBuilder("createTempSeqAuthor", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute("CREATE SEQUENCE IF NOT EXISTS seq_author_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempSeqAuthor() {
        return new StepBuilder("dropTempSeqAuthor", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP SEQUENCE IF EXISTS seq_author_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public Step migrationAuthorStep(
        final RepositoryItemReader<Author> reader,
        final CompositeItemWriter<AuthorDto> writer,
        final AuthorItemProcessorImpl processor
    ) {
        return new StepBuilder("migrationAuthorStep", jobRepository)
            .<Author, AuthorDto>chunk(CHUNK_SIZE, platformTransactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .allowStartIfComplete(true) // позволяет перезапускать шаг, даже если он уже был успешно выполнен

        // todo можно перехватить момент выполнения
            .listener(
                new ItemReadListener<>() {
                    public void beforeRead() {
                        log.info("Начало чтения");
                    }

                    public void afterRead(Author o) {
                        log.info("Конец чтения");
                    }

                    public void onReadError(Exception e) {
                        log.info("Ошибка чтения");
                    }
                }
            )
            .listener(new ItemWriteListener<Author>() {
                public void beforeWrite(List<Author> list) {
                    log.info("Начало записи");
                }

                public void afterWrite(List<Author> list) {
                    log.info("Конец записи");
                }

                public void onWriteError(Exception e, List<Author> list) {
                    log.info("Ошибка записи");
                }
            })
            .listener(new ItemProcessListener<Author,Author>() {
                public void beforeProcess(Author o) {
                    log.info("Начало обработки");
                }

                public void afterProcess(Author o, Author o2) {
                    log.info("Конец обработки");
                }

                public void onProcessError(Author o, Exception e) {
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
}
