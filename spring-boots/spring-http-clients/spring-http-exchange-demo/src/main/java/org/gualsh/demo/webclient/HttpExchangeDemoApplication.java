package org.gualsh.demo.webclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Главный класс Spring Boot приложения для демонстрации Spring WebClient.
 *
 * <p>Аннотации:</p>
 * <ul>
 *   <li>{@link SpringBootApplication} - Комбинирует @Configuration, @EnableAutoConfiguration, @ComponentScan</li>
 *   <li>{@link EnableRetry} - Активирует механизм повторных попыток Spring Retry</li>
 * </ul>
 *
 * <p>Конфигурация включает:</p>
 * <ul>
 *   <li>WebClient для HTTP клиентских запросов</li>
 *   <li>Кэширование с Caffeine провайдером (настроено в CacheConfig)</li>
 *   <li>Retry механизм для обработки временных сбоев</li>
 *   <li>Actuator endpoints для мониторинга</li>
 * </ul>
 */
@SpringBootApplication
@EnableRetry
public class HttpExchangeDemoApplication {

    /**
     * Точка входа в приложение.
     *
     * <p>Запускает Spring Boot контекст с автоконфигурацией
     * и регистрирует все необходимые beans.</p>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(HttpExchangeDemoApplication.class, args);
    }
}