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
import ru.otus.hw.migration.item.processor.GenreItemProcessorImpl;
import ru.otus.hw.model.sourcedb.dto.GenreDto;
import ru.otus.hw.model.sourcedb.entity.Comment;
import ru.otus.hw.model.sourcedb.entity.Genre;

import javax.sql.DataSource;

import java.util.List;

import static ru.otus.hw.migration.job.Job.CHUNK_SIZE;

@Slf4j
@Component
public class GenreStep {

    private final DataSource postgresDataSource;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public GenreStep(
        @Qualifier("postgresDataSource") DataSource postgresDataSource,
        JobRepository jobRepository,
        PlatformTransactionManager platformTransactionManager
    ) {
        this.postgresDataSource = postgresDataSource;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public TaskletStep createTempTableGenre() {
        return new StepBuilder("createTempTableGenre", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "CREATE TABLE temp_table_genre " +
                            "(id_src VARCHAR(255) NOT NULL UNIQUE, id_trg bigint NOT NULL UNIQUE)"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep createTempSeqGenre() {
        return new StepBuilder("createTempSeqGenre", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute("CREATE SEQUENCE IF NOT EXISTS seq_genre_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public Step migrationGenreStep(
        final RepositoryItemReader<Genre> reader,
        final CompositeItemWriter<GenreDto> writer,
        final GenreItemProcessorImpl processor
    ) {
        return new StepBuilder("migrationGenreStep", jobRepository)
            .<Genre, GenreDto>chunk(CHUNK_SIZE, platformTransactionManager)
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

                    public void afterRead(Genre o) {
                        log.info("Конец чтения");
                    }

                    public void onReadError(Exception e) {
                        log.info("Ошибка чтения");
                    }
                }
            )
            .listener(new ItemWriteListener<Genre>() {
                public void beforeWrite(List<Genre> list) {
                    log.info("Начало записи");
                }

                public void afterWrite(List<Genre> list) {
                    log.info("Конец записи");
                }

                public void onWriteError(Exception e, List<Genre> list) {
                    log.info("Ошибка записи");
                }
            })
            .listener(new ItemProcessListener<Genre,Genre>() {
                public void beforeProcess(Genre o) {
                    log.info("Начало обработки");
                }

                public void afterProcess(Genre o, Genre o2) {
                    log.info("Конец обработки");
                }

                public void onProcessError(Genre o, Exception e) {
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
    public TaskletStep dropTempTableGenre() {
        return new StepBuilder("dropTempTableGenre", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "DROP TABLE temp_table_genre"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempSeqGenre() {
        return new StepBuilder("dropTempSeqGenre", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP SEQUENCE IF EXISTS seq_genre_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }
}
