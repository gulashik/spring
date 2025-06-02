package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringPostgresApplication {
    public static void main(String[] args) {
        // Запускаемся из ACTIONS.md
        SpringApplication.run(SpringPostgresApplication.class, args);
    }
}
