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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.migration.item.processor.CommentItemProcessorImpl;
import ru.otus.hw.model.sourcedb.dto.CommentDto;
import ru.otus.hw.model.sourcedb.entity.Comment;

import javax.sql.DataSource;

import java.util.List;

import static ru.otus.hw.migration.job.Job.CHUNK_SIZE;

@Slf4j
@Component
public class CommentStep {

    private final DataSource postgresDataSource;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public CommentStep(
        @Qualifier("postgresDataSource") DataSource postgresDataSource,
        JobRepository jobRepository,
        PlatformTransactionManager platformTransactionManager
    ) {
        this.postgresDataSource = postgresDataSource;
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public TaskletStep createTempTableComment() {
        return new StepBuilder("createTempTableComment", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "CREATE TABLE temp_table_comment " +
                            "(id_src VARCHAR(255) NOT NULL UNIQUE, id_trg BIGINT NOT NULL UNIQUE)"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep createTempSeqComment() {
        return new StepBuilder("createTempSeqComment", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute("CREATE SEQUENCE IF NOT EXISTS seq_comment_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public Step migrationCommentStep(
        final RepositoryItemReader<Comment> reader,
        final CompositeItemWriter<CommentDto> writer,
        final CommentItemProcessorImpl processor
    ) {
        return new StepBuilder("migrationCommentStep", jobRepository)
            .<Comment, CommentDto>chunk(CHUNK_SIZE, platformTransactionManager)
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

                    public void afterRead(Comment o) {
                        log.info("Конец чтения");
                    }

                    public void onReadError(Exception e) {
                        log.info("Ошибка чтения");
                    }
                }
            )
            .listener(new ItemWriteListener<Comment>() {
                public void beforeWrite(List<Comment> list) {
                    log.info("Начало записи");
                }

                public void afterWrite(List<Comment> list) {
                    log.info("Конец записи");
                }

                public void onWriteError(Exception e, List<Comment> list) {
                    log.info("Ошибка записи");
                }
            })
            .listener(new ItemProcessListener<Comment,Comment>() {
                public void beforeProcess(Comment o) {
                    log.info("Начало обработки");
                }

                public void afterProcess(Comment o, Comment o2) {
                    log.info("Конец обработки");
                }

                public void onProcessError(Comment o, Exception e) {
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
    public TaskletStep dropTempTableComment() {
        return new StepBuilder("dropTempTableComment", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        "DROP TABLE temp_table_comment"
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }

    @Bean
    public TaskletStep dropTempSeqComment() {
        return new StepBuilder("dropTempSeqComment", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource)
                        .execute("DROP SEQUENCE IF EXISTS seq_comment_tmp");
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }
}
