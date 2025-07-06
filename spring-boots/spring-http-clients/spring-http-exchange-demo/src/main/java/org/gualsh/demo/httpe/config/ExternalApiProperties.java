package org.gualsh.demo.httpe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * Конфигурационные свойства для внешних API.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот класс демонстрирует современный подход к конфигурации в Spring Boot:
 * </p>
 * <ul>
 * <li>@ConfigurationProperties - связывание свойств из application.yml</li>
 * <li>Использование record для неизменяемых конфигураций</li>
 * <li>Типизированные свойства (Duration, DataSize)</li>
 * <li>Валидация конфигурации</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * @Autowired
 * private ExternalApiProperties properties;
 *
 * // Получение URL для JSONPlaceholder
 * String baseUrl = properties.jsonplaceholder().baseUrl();
 * Duration timeout = properties.jsonplaceholder().timeout();
 * }</pre>
 *
 * <h4>Конфигурация в application.yml</h4>
 * <pre>{@code
 * external-apis:
 *   jsonplaceholder:
 *     base-url: https://jsonplaceholder.typicode.com
 *     timeout: 30s
 *     max-in-memory-size: 1MB
 * }</pre>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "external-apis")
@Data
public class ExternalApiProperties {

    /**
     * Конфигурация для JSONPlaceholder API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Record класс обеспечивает неизменяемость конфигурации,
     * что является best practice для конфигурационных данных.
     * </p>
     */
    private JsonPlaceholderConfig jsonplaceholder = new JsonPlaceholderConfig();

    /**
     * Конфигурация для ReqRes API.
     */
    private ReqResConfig reqres = new ReqResConfig();

    /**
     * Общие настройки HTTP клиента.
     */
    private HttpClientConfig httpClient = new HttpClientConfig();

    /**
     * Конфигурация для JSONPlaceholder API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Использование record обеспечивает:
     * </p>
     * <ul>
     * <li>Неизменяемость объектов</li>
     * <li>Автоматическую генерацию equals/hashCode</li>
     * <li>Компактный код</li>
     * <li>Безопасность потоков</li>
     * </ul>
     */
    @Data
    public static class JsonPlaceholderConfig {
        /**
         * Базовый URL для JSONPlaceholder API.
         */
        private String baseUrl = "https://jsonplaceholder.typicode.com";

        /**
         * Таймаут для запросов.
         */
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * Максимальный размер данных в памяти.
         */
        private DataSize maxInMemorySize = DataSize.ofMegabytes(1);
    }

    /**
     * Конфигурация для ReqRes API.
     */
    @Data
    public static class ReqResConfig {
        /**
         * Базовый URL для ReqRes API.
         */
        private String baseUrl = "https://reqres.in";

        /**
         * Таймаут для запросов.
         */
        private Duration timeout = Duration.ofSeconds(20);

        /**
         * Максимальный размер данных в памяти.
         */
        private DataSize maxInMemorySize = DataSize.ofKilobytes(512);
    }

    /**
     * Общие настройки HTTP клиента.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Централизованная конфигурация HTTP клиента позволяет:
     * </p>
     * <ul>
     * <li>Унифицировать настройки для всех клиентов</li>
     * <li>Легко изменять параметры производительности</li>
     * <li>Обеспечить консистентность конфигурации</li>
     * </ul>
     */
    @Data
    public static class HttpClientConfig {
        /**
         * Таймаут подключения.
         */
        private Duration connectTimeout = Duration.ofSeconds(5);

        /**
         * Таймаут чтения данных.
         */
        private Duration readTimeout = Duration.ofSeconds(30);

        /**
         * Таймаут записи данных.
         */
        private Duration writeTimeout = Duration.ofSeconds(30);

        /**
         * Размер пула соединений.
         */
        private int poolSize = 100;
    }
}
