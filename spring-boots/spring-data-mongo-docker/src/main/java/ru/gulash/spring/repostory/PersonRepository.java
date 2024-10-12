package ru.gulash.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gulash.spring.domain.Person;

import java.util.List;


public interface PersonRepository
    // extends CrudRepository<Person, Integer>
    extends MongoRepository<Person/*entity*/, Integer/*тип ключа*/> // todo наследуем MongoRepository для удобной работы
{

    List<Person> findAll();
}
