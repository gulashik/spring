package ru.otus.hw.migration.step;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Component
public class AllTruncateStep {

    private final DataSource postgresDataSource;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    public AllTruncateStep(
        @Qualifier("postgresDataSource") DataSource postgresDataSource,
        PlatformTransactionManager platformTransactionManager,
        JobRepository jobRepository
    ) {
        this.postgresDataSource = postgresDataSource;
        this.platformTransactionManager = platformTransactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    public TaskletStep truncateTargetTables() {
        return new StepBuilder("truncateTargetTables", jobRepository)
            .allowStartIfComplete(true)
            .tasklet(
                (contribution, chunkContext) -> {
                    new JdbcTemplate(postgresDataSource).execute(
                        """
                            BEGIN TRANSACTION;
                            TRUNCATE AUTHORS CASCADE;
                            TRUNCATE BOOKS CASCADE;
                            TRUNCATE COMMENTS CASCADE;
                            TRUNCATE GENRES CASCADE;
                            COMMIT;
                            """
                    );
                    return RepeatStatus.FINISHED;
                }
                , platformTransactionManager
            )
            .build();
    }
}
