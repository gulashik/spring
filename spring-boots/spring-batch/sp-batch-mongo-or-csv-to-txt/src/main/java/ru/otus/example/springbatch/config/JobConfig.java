package ru.otus.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;


@RequiredArgsConstructor
@Configuration
public class JobConfig {
    public static final String IMPORT_USER_JOB_NAME = "importUserJob";

    private final Logger logger = LoggerFactory.getLogger("Batch");

    private final JobRepository jobRepository;

    @Bean
    public Job importUserJob(
        Step transformPersonsStep, // todo Step with Chunk
        Step cleanUpStep // todo Tasklet Step
    ) {
        return new JobBuilder(IMPORT_USER_JOB_NAME, jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(transformPersonsStep)
            .next(cleanUpStep)
            .end()
            // todo Listener-ы можно врезаться в нужный момент
            .listener(
                new JobExecutionListener() {
                    @Override
                    public void beforeJob(@NonNull JobExecution jobExecution) {
                        logger.info("Начало job");
                    }

                    @Override
                    public void afterJob(@NonNull JobExecution jobExecution) {
                        logger.info("Конец job");
                    }
                }
            )
            .build();
    }
}