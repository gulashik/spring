package ru.gulash.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@RequiredArgsConstructor
@SpringBootApplication
public class JakartaExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(JakartaExampleApplication.class, args);
    }
}
