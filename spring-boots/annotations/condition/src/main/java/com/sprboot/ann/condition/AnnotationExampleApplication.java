package com.sprboot.ann.condition;

import com.sprboot.ann.condition.beans.Person;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AnnotationExampleApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnnotationExampleApplication.class, args);

        context.getBeansOfType(Person.class)
                .values()
                .forEach(Person::sayHello);
    }
}
