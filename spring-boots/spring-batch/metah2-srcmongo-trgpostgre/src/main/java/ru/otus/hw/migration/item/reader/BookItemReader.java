package ru.otus.hw.migration.item.reader;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.entity.Book;
import ru.otus.hw.repositories.mongo.MongoBookRepository;

import java.util.HashMap;

@Component
public class BookItemReader {

    private final MongoBookRepository mongoBookRepository;

    public BookItemReader(MongoBookRepository mongoBookRepository) {
        this.mongoBookRepository = mongoBookRepository;
    }

    @Bean
    public RepositoryItemReader<Book> bookReader() {
        return new RepositoryItemReaderBuilder<Book>()
            .name("bookReader")
            .repository(mongoBookRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(new HashMap<>())
            .build();
    }
}
