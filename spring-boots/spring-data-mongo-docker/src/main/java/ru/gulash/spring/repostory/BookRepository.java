package ru.gulash.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gulash.spring.domain.Book;

public interface BookRepository extends MongoRepository<Book, String> {
    // Дополнительные методы репозитория можно добавлять сюда
}
