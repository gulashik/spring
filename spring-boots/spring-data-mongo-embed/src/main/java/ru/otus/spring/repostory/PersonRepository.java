package ru.otus.spring.repostory;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import ru.otus.spring.domain.Person;

import java.util.List;


public interface PersonRepository
    // extends CrudRepository<Person, Integer>
    extends MongoRepository<Person/*entity*/, Integer/*тип ключа*/> // todo наследуем MongoRepository для удобной работы
{

    List<Person> findAll();
}
