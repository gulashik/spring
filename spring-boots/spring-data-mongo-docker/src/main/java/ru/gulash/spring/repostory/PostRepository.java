package ru.gulash.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gulash.spring.domain.Post;

public interface PostRepository
    /*наследуемся от */
    extends MongoRepository<Post/*тип элемента*/, String/*ключ*/> {
    public Post findByContentRegex(String template);
}