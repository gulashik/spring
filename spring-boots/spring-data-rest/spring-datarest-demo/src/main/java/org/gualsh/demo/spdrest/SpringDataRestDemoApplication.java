package org.gualsh.demo.spdrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной класс приложения Spring Boot.
 * Аннотация @SpringBootApplication объединяет:
 * - @Configuration: помечает класс как источник определений бинов
 * - @EnableAutoConfiguration: включает автоконфигурацию Spring Boot
 * - @ComponentScan: включает сканирование пакетов для поиска компонентов
 */
@SpringBootApplication
public class SpringDataRestDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDataRestDemoApplication.class, args);
    }
}
