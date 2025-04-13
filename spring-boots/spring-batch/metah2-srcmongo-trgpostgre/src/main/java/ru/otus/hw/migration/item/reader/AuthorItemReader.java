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

    public AuthorItemReader(
        MongoAuthorRepository mongoAuthorRepository // todo репозиторий откуда читаем
    ) {
        this.mongoAuthorRepository = mongoAuthorRepository;
    }

    @Bean
    public RepositoryItemReader<Author> authorReader() {
        return new RepositoryItemReaderBuilder<Author>()
            .name("authorReader") // уникальное имя reader
            .repository(mongoAuthorRepository) // репозиторий для чтения данных.
            .methodName("findAll") // метод репозитория, который будет вызываться для получения данных
            .pageSize(10) // размер страницы для постраничного чтения
            .sorts(new HashMap<>()) // пустая Map сортировки, что означает использование сортировки по умолчанию
            .build();
    }
}
