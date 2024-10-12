package ru.gulash.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gulash.spring.domain.Author;

public interface AuthorRepository extends MongoRepository<Author, String> {
    // Дополнительные методы репозитория можно добавлять сюда
}
