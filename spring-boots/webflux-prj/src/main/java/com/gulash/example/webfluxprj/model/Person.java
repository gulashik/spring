package com.gulash.example.webfluxprj.model;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@Getter
@Table("person")
public class Person {

    @Id
    private final Long id;

    private final String lastName;

    private final int age;

    @PersistenceCreator
    private Person(Long id, String lastName, int age) {
        this.id = id;
        this.lastName = lastName;
        this.age = age;
    }
    public Person(String lastName, int age) {
        this(null, lastName,  age);
    }
}
