package org.gualsh.demo.gw.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Конфигурационные свойства для Spring Cloud Gateway Demo.
 *
 * <p><strong>Образовательный момент:</strong>
 * Использование @ConfigurationProperties обеспечивает:
 * <ul>
 * <li>Type-safe конфигурацию</li>
 * <li>Автодополнение в IDE</li>
 * <li>Валидацию конфигурации</li>
 * <li>Документацию свойств</li>
 * <li>Hierarchical конфигурацию</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * # В application.yml
 * gateway:
 *   demo:
 *     rate-limiting:
 *       enabled: true
 *       default-rate: 100
 *       burst-capacity: 200
 *     security:
 *       enabled: true
 *       allowed-origins:
 *         - https://example.com
 *         - https://test.com
 * }</pre>
 *
 * @author Spring Cloud Gateway Demo
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.demo")
@Validated
public class CustomGatewayProperties {

    /**
     * Включение/отключение demo функций.
     *
     * <p><strong>Образовательный момент:</strong>
     * Feature flags на уровне конфигурации позволяют
     * легко включать/отключать функциональность без изменения кода.
     */
    private boolean enabled = true;

    /**
     * Настройки Rate Limiting.
     *
     * <p><strong>Образовательный момент:</strong>
     * Группировка связанных настроек в отдельные классы
     * улучшает организацию конфигурации.
     */
    @NotNull
    private RateLimiting rateLimiting = new RateLimiting();

    /**
     * Настройки безопасности.
     */
    @NotNull
    private Security security = new Security();

    /**
     * Настройки Circuit Breaker.
     */
    @NotNull
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    /**
     * Настройки мониторинга.
     */
    @NotNull
    private Monitoring monitoring = new Monitoring();

    /**
     * Настройки кэширования.
     */
    @NotNull
    private Caching caching = new Caching();

    /**
     * Настройки логирования.
     */
    @NotNull
    private Logging logging = new Logging();

    /**
     * Конфигурация Rate Limiting.
     *
     * <p><strong>Образовательный момент:</strong>
     * Nested configuration classes позволяют создавать
     * структурированную конфигурацию с валидацией.
     */
    @Data
    public static class RateLimiting {

        /**
         * Включение Rate Limiting.
         */
        private boolean enabled = true;

        /**
         * Скорость пополнения токенов (запросов в секунду).
         */
        @Min(1)
        @Max(10000)
        private int defaultRate = 100;

        /**
         * Максимальная емкость bucket (burst capacity).
         */
        @Min(1)
        @Max(50000)
        private int burstCapacity = 200;

        /**
         * Время блокировки при превышении лимита.
         */
        @NotNull
        private Duration penaltyDuration = Duration.ofMinutes(1);

        /**
         * Настройки по маршрутам.
         */
        private Map<String, RouteRateLimit> routes = Map.of();

        /**
         * Настройки Rate Limiting для конкретного маршрута.
         */
        @Data
        public static class RouteRateLimit {
            @Min(1)
            private int rate = 50;

            @Min(1)
            private int burstCapacity = 100;

            @NotNull
            private Duration penaltyDuration = Duration.ofMinutes(1);
        }
    }

    /**
     * Конфигурация безопасности.
     *
     * <p><strong>Образовательный момент:</strong>
     * Централизованная конфигурация безопасности
     * упрощает управление и аудит настроек.
     */
    @Data
    public static class Security {

        /**
         * Включение security функций.
         */
        private boolean enabled = true;

        /**
         * Разрешенные origins для CORS.
         */
        private List<String> allowedOrigins = List.of("*");

        /**
         * Разрешенные HTTP методы.
         */
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

        /**
         * Разрешенные заголовки.
         */
        private List<String> allowedHeaders = List.of("*");

        /**
         * Время жизни preflight ответа.
         */
        @NotNull
        private Duration preflightMaxAge = Duration.ofHours(1);

        /**
         * Включение аутентификации по API ключу.
         */
        private boolean apiKeyAuthEnabled = false;

        /**
         * Имя заголовка для API ключа.
         */
        @NotBlank
        private String apiKeyHeader = "X-API-Key";

        /**
         * Валидные API ключи.
         */
        private Map<String, String> apiKeys = Map.of();

        /**
         * Включение JWT аутентификации.
         */
        private boolean jwtAuthEnabled = false;

        /**
         * Секретный ключ для JWT.
         */
        private String jwtSecret = "demo-secret-key";

        /**
         * Время жизни JWT токена.
         */
        @NotNull
        private Duration jwtExpiration = Duration.ofHours(24);
    }

    /**
     * Конфигурация Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong>
     * Circuit Breaker критически важен для устойчивости системы
     * и должен быть правильно настроен для каждого сервиса.
     */
    @Data
    public static class CircuitBreaker {

        /**
         * Включение Circuit Breaker.
         */
        private boolean enabled = true;

        /**
         * Пороговое значение ошибок для открытия circuit (в процентах).
         */
        @Min(1)
        @Max(100)
        private int failureRateThreshold = 50;

        /**
         * Время ожидания в открытом состоянии.
         */
        @NotNull
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);

        /**
         * Размер скользящего окна.
         */
        @Min(1)
        @Max(1000)
        private int slidingWindowSize = 10;

        /**
         * Минимальное количество вызовов для расчета статистики.
         */
        @Min(1)
        @Max(100)
        private int minimumNumberOfCalls = 5;

        /**
         * Пороговое значение для медленных вызовов (в процентах).
         */
        @Min(1)
        @Max(100)
        private int slowCallRateThreshold = 50;

        /**
         * Время, после которого вызов считается медленным.
         */
        @NotNull
        private Duration slowCallDurationThreshold = Duration.ofSeconds(2);

        /**
         * Timeout для Circuit Breaker.
         */
        @NotNull
        private Duration timeoutDuration = Duration.ofSeconds(3);
    }

    /**
     * Конфигурация мониторинга.
     *
     * <p><strong>Образовательный момент:</strong>
     * Мониторинг должен быть встроен в Gateway с самого начала
     * для обеспечения наблюдаемости в production.
     */
    @Data
    public static class Monitoring {

        /**
         * Включение мониторинга.
         */
        private boolean enabled = true;

        /**
         * Включение сбора детальных метрик.
         */
        private boolean detailedMetricsEnabled = true;

        /**
         * Включение трассировки запросов.
         */
        private boolean tracingEnabled = true;

        /**
         * Интервал сбора метрик.
         */
        @NotNull
        private Duration metricsInterval = Duration.ofSeconds(30);

        /**
         * Включение health checks.
         */
        private boolean healthChecksEnabled = true;

        /**
         * Timeout для health checks.
         */
        @NotNull
        private Duration healthCheckTimeout = Duration.ofSeconds(5);

        /**
         * Включение экспорта метрик в Prometheus.
         */
        private boolean prometheusEnabled = true;

        /**
         * Включение уведомлений о критических ошибках.
         */
        private boolean alertingEnabled = false;

        /**
         * Webhook для уведомлений.
         */
        private String alertWebhook = "";
    }

    /**
     * Конфигурация кэширования.
     *
     * <p><strong>Образовательный момент:</strong>
     * Кэширование на уровне Gateway может значительно
     * улучшить производительность и снизить нагрузку на backend.
     */
    @Data
    public static class Caching {

        /**
         * Включение кэширования.
         */
        private boolean enabled = false;

        /**
         * Время жизни кэша по умолчанию.
         */
        @NotNull
        private Duration defaultTtl = Duration.ofMinutes(5);

        /**
         * Максимальный размер кэша.
         */
        @Min(1)
        @Max(10000)
        private int maxSize = 1000;

        /**
         * Настройки кэширования по маршрутам.
         */
        private Map<String, RouteCacheConfig> routes = Map.of();

        /**
         * Конфигурация кэширования для маршрута.
         */
        @Data
        public static class RouteCacheConfig {
            @NotNull
            private Duration ttl = Duration.ofMinutes(5);

            private boolean enabled = true;

            private List<String> cacheableHeaders = List.of("Content-Type");

            private List<String> cacheableStatuses = List.of("200", "201", "204");
        }
    }

    /**
     * Конфигурация логирования.
     *
     * <p><strong>Образовательный момент:</strong>
     * Настройка логирования должна быть гибкой для различных
     * окружений (dev, staging, production).
     */
    @Data
    public static class Logging {

        /**
         * Включение детального логирования.
         */
        private boolean detailedLoggingEnabled = true;

        /**
         * Логирование тел запросов.
         */
        private boolean logRequestBodies = false;

        /**
         * Логирование тел ответов.
         */
        private boolean logResponseBodies = false;

        /**
         * Логирование заголовков.
         */
        private boolean logHeaders = true;

        /**
         * Максимальный размер логируемого тела (в байтах).
         */
        @Min(0)
        @Max(1048576) // 1MB
        private int maxBodySize = 1024;

        /**
         * Заголовки для исключения из логирования.
         */
        private List<String> excludeHeaders = List.of(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "X-API-Key"
        );

        /**
         * Уровень логирования для различных компонентов.
         */
        private Map<String, String> logLevels = Map.of(
            "org.springframework.cloud.gateway", "DEBUG",
            "org.gualsh.demo.gw", "DEBUG",
            "reactor.netty", "INFO"
        );
    }
}