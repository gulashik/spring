package ru.gulash.spring.dao;



import ru.gulash.spring.domain.Person;

import java.util.List;

public interface PersonDao {

    int count();

    void insert(Person person);

    Person getById(long id);

    List<Person> getAll();

    void deleteById(long id);
}
