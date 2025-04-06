package ru.otus.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
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


@RequiredArgsConstructor
@Configuration
public class StepConfig {
    public static final String OUTPUT_FILE_NAME = "outputFileName";
    public static final String INPUT_FILE_NAME = "inputFileName";
    private static final int CHUNK_SIZE = 5;

    private final JobRepository jobRepository;

    private final CleanUpService cleanUpService;

    private final PlatformTransactionManager platformTransactionManager;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    // todo по умолчанию будем брать из embedded mongo
    @ConditionalOnProperty(value = "application.source.mongo", matchIfMissing = true)
    @StepScope // todo Можно не использовать т.к. не нужно доставать параметры из StepContext
    @Bean
    public MongoPagingItemReader<Person> readerMongo(MongoTemplate template) {
        Map<String, Sort.Direction> sortMap = Map.of("age",DESC);

        return new MongoPagingItemReaderBuilder<Person>()
            .name("personItemReader")
            .template(template)
            .jsonQuery("{}")
            .targetType(Person.class)
            .pageSize(10)
            .sorts(sortMap)
            .build();
    }

    // todo если не из mongo значит из файла
    @ConditionalOnProperty(value = "application.source.flat-file")
    @Bean
    @StepScope
    public FlatFileItemReader<Person> readerFlatFile(@Value("#{jobParameters['" + StepConfig.INPUT_FILE_NAME + "']}") String inputFileName) {
        return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new FileSystemResource(inputFileName))

            .delimited()
            .names("name", "age")
            .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Person.class);
            }}).build();
    }



    @StepScope
    @Bean
    public ItemProcessor<Person, Person> processor(HappyBirthdayService happyBirthdayService) {
        return happyBirthdayService::doHappyBirthday;
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<Person> writer(@Value("#{jobParameters['" + StepConfig.OUTPUT_FILE_NAME + "']}") String outputFileName) {
        return new FlatFileItemWriterBuilder<Person>()
            .name("personItemWriter")
            .resource(new FileSystemResource(outputFileName))
            .lineAggregator(new DelimitedLineAggregator<>())
            .build();
    }

    @Bean
    public MethodInvokingTaskletAdapter cleanUpTasklet() {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();

        adapter.setTargetObject(cleanUpService);
        adapter.setTargetMethod("cleanUp");

        return adapter;
    }

    @Bean
    public Step cleanUpStep() {
        return new StepBuilder("cleanUpStep", jobRepository)
            .tasklet(cleanUpTasklet(), platformTransactionManager)
            .build();
    }

    @Bean
    public Step transformPersonsStep(ItemReader<Person> reader, FlatFileItemWriter<Person> writer,
                                     ItemProcessor<Person, Person> itemProcessor) {
        return new StepBuilder("transformPersonsStep", jobRepository)
            .<Person, Person>chunk(CHUNK_SIZE, platformTransactionManager)
            .reader(reader)
            .processor(itemProcessor)
            .writer(writer)
            .listener(new ItemReadListener<>() {
                public void beforeRead() {
                    logger.info("Начало чтения");
                }

                public void afterRead(@NonNull Person o) {
                    logger.info("Конец чтения");
                }

                public void onReadError(@NonNull Exception e) {
                    logger.info("Ошибка чтения");
                }
            })
            .listener(new ItemWriteListener<Person>() {
                public void beforeWrite(@NonNull List<Person> list) {
                    logger.info("Начало записи");
                }

                public void afterWrite(@NonNull List<Person> list) {
                    logger.info("Конец записи");
                }

                public void onWriteError(@NonNull Exception e, @NonNull List<Person> list) {
                    logger.info("Ошибка записи");
                }
            })
            .listener(new ItemProcessListener<>() {
                public void beforeProcess(@NonNull Person o) {
                    logger.info("Начало обработки");
                }

                public void afterProcess(@NonNull Person o, Person o2) {
                    logger.info("Конец обработки");
                }

                public void onProcessError(@NonNull Person o, @NonNull Exception e) {
                    logger.info("Ошибка обработки");
                }
            })
            .listener(new ChunkListener() {
                public void beforeChunk(@NonNull ChunkContext chunkContext) {
                    logger.info("Начало пачки");
                }

                public void afterChunk(@NonNull ChunkContext chunkContext) {
                    logger.info("Конец пачки");
                }

                public void afterChunkError(@NonNull ChunkContext chunkContext) {
                    logger.info("Ошибка пачки");
                }
            })
//                .taskExecutor(new SimpleAsyncTaskExecutor())
            .build();
    }
}
