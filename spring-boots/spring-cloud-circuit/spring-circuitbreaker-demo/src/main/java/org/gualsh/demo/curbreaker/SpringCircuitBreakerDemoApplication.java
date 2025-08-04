package org.gualsh.demo.curbreaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения для демонстрации Spring Cloud Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данное приложение демонстрирует использование паттерна Circuit Breaker в Spring Boot
 * с помощью Spring Cloud Circuit Breaker и Resilience4j. Circuit Breaker помогает
 * предотвратить каскадные сбои в распределенных системах.
 * </p>
 *
 * <p><strong>Пример запуска:</strong></p>
 * <pre>{@code
 * java -jar spring-circuit-breaker-demo-1.0.0.jar
 * // или через Maven
 * mvn spring-boot:run
 * }</pre>
 *
 * <p><strong>Основные возможности приложения:</strong></p>
 * <ul>
 *   <li>Демонстрация различных конфигураций Circuit Breaker</li>
 *   <li>Интеграция с внешними API через WebClient</li>
 *   <li>Мониторинг метрик через Actuator endpoints</li>
 *   <li>Fallback механизмы для graceful degradation</li>
 * </ul>
 *
 * @author Educational Demo
 * @version 1.0.0
 * @since Spring Boot 3.3.4
 */
@SpringBootApplication
public class SpringCircuitBreakerDemoApplication {

    /**
     * Точка входа в приложение.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * @SpringBootApplication - это мета-аннотация, которая включает:
     * - @Configuration: класс как источник bean definitions
     * - @EnableAutoConfiguration: автоматическая конфигурация Spring Boot
     * - @ComponentScan: сканирование компонентов в текущем пакете и подпакетах
     * </p>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringCircuitBreakerDemoApplication.class, args);
    }
}