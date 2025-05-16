package org.gualsh.demo.micromet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Основной класс приложения.
 *
 * @EnableScheduling - включает поддержку для запланированных задач, которые используются
 * для генерации тестовых метрик
 */
@SpringBootApplication
@EnableScheduling
public class MicrometApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicrometApplication.class, args);
    }
}