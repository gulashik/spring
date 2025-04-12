package ru.otus.hw.migration.item.reader;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.entity.Genre;
import ru.otus.hw.repositories.mongo.MongoGenreRepository;

import java.util.HashMap;

@Component
public class GenreItemReader {

    private final MongoGenreRepository mongoGenreRepository;

    public GenreItemReader(MongoGenreRepository mongoGenreRepository) {
        this.mongoGenreRepository = mongoGenreRepository;
    }

    @Bean
    public RepositoryItemReader<Genre> genreReader() {
        return new RepositoryItemReaderBuilder<Genre>()
            .name("genreReader")
            .repository(mongoGenreRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(new HashMap<>())
            .build();
    }
}
