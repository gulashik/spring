package org.gualsh.demo.restclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Главный класс демонстрационного приложения Spring RestClient.
 *
 *
 */
@SpringBootApplication
@EnableCaching      // Включает поддержку кеширования для оптимизации запросов
@EnableRetry        // Включает механизм автоматических повторов при ошибках
@EnableAsync        // Включает асинхронное выполнение методов
public class RestClientDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestClientDemoApplication.class, args);
    }
}