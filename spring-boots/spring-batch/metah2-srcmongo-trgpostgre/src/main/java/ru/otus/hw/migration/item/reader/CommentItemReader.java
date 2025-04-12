package ru.otus.hw.migration.item.reader;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.otus.hw.model.sourcedb.entity.Comment;
import ru.otus.hw.repositories.mongo.MongoCommentRepository;

import java.util.HashMap;

@Component
public class CommentItemReader {

    private final MongoCommentRepository mongoCommentRepository;

    public CommentItemReader(MongoCommentRepository mongoCommentRepository) {
        this.mongoCommentRepository = mongoCommentRepository;
    }

    @Bean
    public RepositoryItemReader<Comment> commentReader() {
        return new RepositoryItemReaderBuilder<Comment>()
            .name("commentReader")
            .repository(mongoCommentRepository)
            .methodName("findAll")
            .pageSize(10)
            .sorts(new HashMap<>())
            .build();
    }
}
