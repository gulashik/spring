package ru.otus.hw.migration.item.reader;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.entity.Author;
import ru.otus.hw.repositories.mongo.MongoAuthorRepository;

import java.util.HashMap;

@Component
public class AuthorItemReader {

    private final MongoAuthorRepository mongoAuthorRepository;

    public AuthorItemReader(MongoAuthorRepository mongoAuthorRepository) {
        this.mongoAuthorRepository = mongoAuthorRepository;
    }

    @Bean
    public RepositoryItemReader<Author> authorReader() {
        return new RepositoryItemReaderBuilder<Author>()
            .name("authorReader")
            .repository(mongoAuthorRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(new HashMap<>())
            .build();
    }
}
