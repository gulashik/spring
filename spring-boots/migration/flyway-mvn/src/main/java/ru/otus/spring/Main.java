package ru.otus.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// todo Перейти url h2 консоли: http://localhost:8080/h2-console
// todo Подставить в url базы: jdbc:h2:mem:testdb

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
