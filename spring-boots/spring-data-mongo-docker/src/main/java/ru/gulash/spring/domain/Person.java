package ru.gulash.spring.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


/*todo имя документа*/
@Document(collection = "persons")

public class Person {

    @Id
    private String id;

    @Indexed // в данном случае индекса не будет т.к. коллекция пустая(пересоздаётся при каждом запуске)
    @Field(name = "first_name") // указываем, что поле name в классе соответствует first_name в MongoDB
    private String name;

    @Indexed(unique = false, direction = IndexDirection.ASCENDING)
    private String email;

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            '}';
    }
}

