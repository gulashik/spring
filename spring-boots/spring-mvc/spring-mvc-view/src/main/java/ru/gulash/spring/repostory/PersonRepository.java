package ru.gulash.spring.repostory;

import org.springframework.data.repository.CrudRepository;
import ru.gulash.spring.domain.Person;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findAll();
}
