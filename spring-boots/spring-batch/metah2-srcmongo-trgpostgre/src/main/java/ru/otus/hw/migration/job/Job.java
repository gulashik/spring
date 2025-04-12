package ru.otus.hw.migration.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.migration.step.*;

@Configuration
public class Job {
    public static final String MIGRATE_JOB_NAME = "migrateJob";

    public static final int CHUNK_SIZE = 5;

    private final JobRepository jobRepository;

    private final AllTruncateStep allTruncateStep;

    private final AuthorStep authorStep;

    private final BookStep bookStep;

    private final GenreStep genreStep;

    private final CommentStep commentStep;

    public Job(
        JobRepository jobRepository,
        AllTruncateStep allTruncateStep,
        AuthorStep authorStep,
        BookStep bookStep,
        GenreStep genreStep,
        CommentStep commentStep
    ) {
        this.jobRepository = jobRepository;
        this.allTruncateStep = allTruncateStep;
        this.authorStep = authorStep;
        this.bookStep = bookStep;
        this.genreStep = genreStep;
        this.commentStep = commentStep;
    }

    @Bean
    public org.springframework.batch.core.Job migrateJob(
        final Step migrationAuthorStep,
        final Step migrationGenreStep,
        final Step migrationCommentStep,
        final Step migrationBookStep
    ) {
        return new JobBuilder(MIGRATE_JOB_NAME, jobRepository)
            // RunIdIncrementer increments run.id param each time the Job is running
            // Позволяет перезапускать Job с одинаковыми параметрами одним и тем же экземпляром (JobInstance)
            .incrementer(new RunIdIncrementer())

            // Pre-cleaning target tables
            .start(allTruncateStep.truncateTargetTables())

            // Temporary objects create
            // Table
            .next(authorStep.createTempTableAuthor())
            .next(genreStep.createTempTableGenre())
            .next(bookStep.createTempTableBook())
            .next(commentStep.createTempTableComment())
            // Sequence
            .next(authorStep.createTempSeqAuthor())
            .next(genreStep.createTempSeqGenre())
            .next(bookStep.createTempSeqBook())
            .next(commentStep.createTempSeqComment())

            // Migration
            .next(migrationAuthorStep)
            .next(migrationGenreStep)
            .next(migrationBookStep)
            .next(migrationCommentStep)

            // Temporary objects drop
            // Table
            .next(authorStep.dropTempTableAuthor())
            .next(genreStep.dropTempTableGenre())
            .next(bookStep.dropTempTableBook())
            .next(commentStep.dropTempTableComment())
            // Sequence
            .next(authorStep.dropTempSeqAuthor())
            .next(genreStep.dropTempSeqGenre())
            .next(bookStep.dropTempSeqBook())
            .next(commentStep.dropTempSeqComment())

            .build();
        /*
        SELECT * FROM BATCH_JOB_EXECUTION ;
        SELECT * FROM BATCH_JOB_EXECUTION_CONTEXT ;
        SELECT * FROM BATCH_JOB_EXECUTION_PARAMS ;
        SELECT * FROM BATCH_JOB_INSTANCE ;
        SELECT * FROM BATCH_STEP_EXECUTION ;
        SELECT * FROM BATCH_STEP_EXECUTION_CONTEXT ;
        */
    }
}
