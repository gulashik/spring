package org.gualsh.demo.micromet;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Основной класс приложения, демонстрирующего возможности Micrometer.
 * <p>
 * Данное приложение показывает различные способы использования Micrometer для
 * мониторинга приложения Spring Boot, включая встроенные метрики, создание
 * пользовательских метрик и интеграцию с различными системами мониторинга.
 * </p>
 *
 * @author gualsh
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling  // Включаем планирование для демонстрационных задач
public class MicrometerDemoApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(MicrometerDemoApplication.class, args);
    }
}