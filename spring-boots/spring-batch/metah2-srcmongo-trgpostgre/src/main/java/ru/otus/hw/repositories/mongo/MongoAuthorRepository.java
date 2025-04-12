package ru.otus.hw.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.model.sourcedb.entity.Author;

public interface MongoAuthorRepository extends MongoRepository<Author, String> {
}
