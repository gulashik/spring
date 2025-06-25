package org.gualsh.demo.openfeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Главный класс Spring Boot приложения для демонстрации Spring OpenFeign.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Аннотация {@code @EnableFeignClients} активирует поддержку OpenFeign в Spring контексте.
 * Это ключевая аннотация, без которой Feign клиенты не будут обнаружены и зарегистрированы
 * как Spring бины.
 * </p>
 *
 * <h3>Основные возможности @EnableFeignClients:</h3>
 * <ul>
 *   <li><strong>basePackages</strong> - указывает пакеты для сканирования Feign интерфейсов</li>
 *   <li><strong>clients</strong> - явно указывает классы Feign клиентов</li>
 *   <li><strong>defaultConfiguration</strong> - глобальная конфигурация для всех клиентов</li>
 * </ul>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Размещайте все Feign интерфейсы в отдельном пакете для лучшей организации</li>
 *   <li>Используйте basePackages для ограничения области сканирования</li>
 *   <li>Всегда настраивайте таймауты и retry политики</li>
 * </ul>
 */
@SpringBootApplication
@EnableFeignClients( // включает поддержку OpenFeign в Spring
    // Указываем базовый пакет для поиска Feign интерфейсов
    // Это улучшает производительность старта приложения, так как сканируется
    // только указанный пакет, а не все classpath
    basePackages = "org.gualsh.demo.openfeign.client"
)
/*@EnableFeignClients(
    // Указание пакетов для сканирования
    basePackages = {"com.example.clients", "com.example.external"},

    // Указание классов как базовых пакетов (type-safe альтернатива)
    basePackageClasses = {ClientsMarker.class, ExternalClientsMarker.class},

    // Явное указание конкретных клиентов
    clients = {UserServiceClient.class, PaymentServiceClient.class},

    // Значение по умолчанию (сокращение для basePackages)
    value = {"com.example.clients"},

    // Глобальная конфигурация по умолчанию для всех клиентов
    defaultConfiguration = GlobalFeignConfiguration.class
)*/
public class SpringOpenfeignDemoApplication {

    /**
     * Точка входа в приложение.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * При запуске Spring Boot приложения с OpenFeign происходит следующее:
     * </p>
     * <ol>
     *   <li>Сканирование интерфейсов с аннотацией @FeignClient</li>
     *   <li>Создание прокси-объектов для каждого Feign клиента</li>
     *   <li>Регистрация прокси как Spring бинов</li>
     *   <li>Применение конфигурации из application.yml</li>
     *   <li>Инициализация HTTP клиента (по умолчанию или OkHttp)</li>
     * </ol>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        // todo запусккаем ACTIONS.md
        SpringApplication.run(SpringOpenfeignDemoApplication.class, args);
    }
}