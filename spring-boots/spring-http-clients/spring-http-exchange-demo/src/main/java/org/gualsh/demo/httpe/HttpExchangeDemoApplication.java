package org.gualsh.demo.httpe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Главный класс для демонстрации Spring @HttpExchange возможностей.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот класс демонстрирует правильную настройку Spring Boot приложения
 * для работы с @HttpExchange аннотациями. Ключевые аспекты:
 * </p>
 * <ul>
 * <li>@SpringBootApplication - основная аннотация для Spring Boot</li>
 * <li>@ConfigurationPropertiesScan - автоматическое сканирование конфигурационных классов</li>
 * <li>Использование WebFlux для реактивного программирования</li>
 * </ul>
 *
 * <h4>Пример запуска</h4>
 * <pre>{@code
 * // Запуск из IDE
 * public static void main(String[] args) {
 *     SpringApplication.run(HttpExchangeDemoApplication.class, args);
 * }
 *
 * // Запуск с дополнительными параметрами
 * SpringApplication app = new SpringApplication(HttpExchangeDemoApplication.class);
 * app.setAdditionalProfiles("dev");
 * app.run(args);
 * }</pre>
 *
 * <h4>Техническая информация</h4>
 * <p>
 * Приложение использует Spring Boot 3.3.4+ с поддержкой:
 * </p>
 * <ul>
 * <li>WebFlux для реактивного программирования</li>
 * <li>WebClient как HTTP-клиент</li>
 * <li>Jackson для JSON сериализации</li>
 * <li>Lombok для уменьшения boilerplate кода</li>
 * <li>Validation для валидации данных</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 * @since Spring Boot 3.3.4
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class HttpExchangeDemoApplication {

    /**
     * Главный метод для запуска Spring Boot приложения.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * SpringApplication.run() выполняет следующие действия:
     * </p>
     * <ol>
     * <li>Создает ApplicationContext</li>
     * <li>Регистрирует все бины, включая @HttpExchange прокси</li>
     * <li>Запускает встроенный веб-сервер</li>
     * <li>Выполняет автоконфигурацию</li>
     * </ol>
     *
     * <pre>{@code
     * // Эквивалентный код с настройкой
     * SpringApplication app = new SpringApplication(HttpExchangeDemoApplication.class);
     * app.setBannerMode(Banner.Mode.CONSOLE);
     * app.setLogStartupInfo(true);
     * app.run(args);
     * }</pre>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(HttpExchangeDemoApplication.class, args);
    }
}
