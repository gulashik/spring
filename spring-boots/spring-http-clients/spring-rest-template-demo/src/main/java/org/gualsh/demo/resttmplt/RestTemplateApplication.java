package org.gualsh.demo.resttmplt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Главный класс приложения для демонстрации возможностей Spring RestTemplate.
 *
 * <p>Включает следующие функциональности:</p>
 * <ul>
 *   <li>@EnableCaching - активирует поддержку кэширования для уменьшения количества HTTP запросов</li>
 *   <li>@EnableRetry - активирует механизм повторных попыток при сбоях</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableRetry
public class RestTemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestTemplateApplication.class, args);
    }
}
