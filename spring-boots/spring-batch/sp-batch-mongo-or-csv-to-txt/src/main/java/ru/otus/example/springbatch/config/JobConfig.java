package ru.otus.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.example.springbatch.model.Person;
import ru.otus.example.springbatch.service.CleanUpService;
import ru.otus.example.springbatch.service.HappyBirthdayService;

import java.util.List;
import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.DESC;


@SuppressWarnings("unused")
@RequiredArgsConstructor
@Configuration
public class JobConfig {
    public static final String IMPORT_USER_JOB_NAME = "importUserJob";

    private final Logger logger = LoggerFactory.getLogger("Batch");

    private final JobRepository jobRepository;

    @Bean
    public Job importUserJob(Step transformPersonsStep, Step cleanUpStep) {
        return new JobBuilder(IMPORT_USER_JOB_NAME, jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(transformPersonsStep)
            .next(cleanUpStep)
            .end()
            .listener(new JobExecutionListener() {
                @Override
                public void beforeJob(@NonNull JobExecution jobExecution) {
                    logger.info("Начало job");
                }

                @Override
                public void afterJob(@NonNull JobExecution jobExecution) {
                    logger.info("Конец job");
                }
            })
            .build();
    }
}