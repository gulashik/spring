package org.gualsh.demo.gw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Главный класс приложения Spring Cloud Gateway Demo.
 *
 * <p><strong>Образовательный момент:</strong>
 * Этот класс демонстрирует основные принципы создания Gateway приложения:
 * <ul>
 * <li>Использование @SpringBootApplication для автоконфигурации</li>
 * <li>Программное создание маршрутов через RouteLocator</li>
 * <li>Интеграция с Spring Cloud Gateway</li>
 * <li>Создание fallback endpoints</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * // Запуск приложения
 * java -jar target/spring-cloud-gateway-demo-1.0.0.jar
 *
 * // Тестирование маршрутов
 * curl -X GET http://localhost:8080/demo/get
 * curl -X GET http://localhost:8080/programmatic/get
 * curl -X GET http://localhost:8080/fallback
 * }</pre>
 *
 * @author Spring Cloud Gateway Demo
 * @since 1.0.0
 */
@SpringBootApplication
@RestController
public class GatewayDemoApplication {

    /**
     * Точка входа в приложение.
     *
     * <p><strong>Образовательный момент:</strong>
     * Spring Boot автоматически настраивает Spring Cloud Gateway
     * благодаря наличию spring-cloud-starter-gateway в classpath.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }

    /**
     * Программное создание маршрутов через RouteLocator.
     *
     * <p><strong>Образовательный момент:</strong>
     * Программное создание маршрутов предпочтительнее конфигурационного
     * в следующих случаях:
     * <ul>
     * <li>Сложная логика предикатов</li>
     * <li>Динамическое создание маршрутов</li>
     * <li>Интеграция с внешними системами</li>
     * <li>Кастомная логика фильтров</li>
     * </ul>
     *
     * <p><strong>Пример использования:</strong>
     * <pre>{@code
     * curl -X GET http://localhost:8080/programmatic/get
     * curl -X POST http://localhost:8080/programmatic/post -d '{"key":"value"}'
     * curl -X GET "http://localhost:8080/conditional/get?env=dev"
     * }</pre>
     *
     * @param builder конструктор маршрутов
     * @return настроенный RouteLocator
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Базовый программный маршрут
            .route("programmatic-route", r -> r
                .path("/programmatic/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Programmatic-Route", "true")
                    .addResponseHeader("X-Custom-Header", "Programmatic-Route")
                    .addRequestParameter("source", "gateway")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с условной логикой
            .route("conditional-route", r -> r
                .path("/conditional/**")
                .and()
                .query("env", "dev|staging")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Environment", "development")
                    .modifyRequestBody(String.class, String.class,
                        (exchange, body) -> {
                            // Добавляем metadata для dev окружения
                            return Mono.just(body != null ?
                                body + "\n// Added by Gateway for dev environment" :
                                "// Added by Gateway for dev environment");
                        }
                    )
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с кастомными предикатами
            .route("custom-predicate-route", r -> r
                .path("/custom/**")
                .and()
                .header("X-Custom-Header")
                .and()
                .method("GET", "POST")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Custom-Predicate", "matched")
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(exchange ->
                            Mono.just(exchange.getRequest()
                                .getHeaders()
                                .getFirst("X-Custom-Header")
                            )
                        )
                    )
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с обработкой ошибок
            .route("error-handling-route", r -> r
                .path("/error-test/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("error-test-cb")
                        .setFallbackUri("forward:/fallback")
                    )
                    .retry(config -> config
                        .setRetries(2)
                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY)
                    )
                )
                .uri("https://httpbin.org")
            )

            .build();
    }

    /**
     * Создание Redis Rate Limiter для демонстрации.
     *
     * <p><strong>Образовательный момент:</strong>
     * Redis Rate Limiter использует токен bucket алгоритм
     * для ограничения количества запросов. Важные параметры:
     * <ul>
     * <li>replenishRate - скорость пополнения токенов</li>
     * <li>burstCapacity - максимальное количество токенов</li>
     * <li>requestedTokens - количество токенов на запрос</li>
     * </ul>
     *
     * @return настроенный RedisRateLimiter
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(
            5,  // replenishRate: 5 запросов в секунду
            10  // burstCapacity: максимум 10 запросов в burst
        );
    }

    /**
     * Fallback endpoint для Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong>
     * Fallback endpoints должны быть быстрыми и надежными.
     * Избегайте сложной логики и внешних вызовов в fallback.
     *
     * <p><strong>Пример использования:</strong>
     * <pre>{@code
     * // Прямой вызов fallback
     * curl -X GET http://localhost:8080/fallback
     *
     * // Автоматический fallback при ошибке в circuit breaker
     * curl -X GET http://localhost:8080/circuit-breaker/status/500
     * }</pre>
     *
     * @return сообщение о недоступности сервиса
     */
    @GetMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("Service temporarily unavailable. Please try again later.");
    }

    /**
     * Health check endpoint для мониторинга.
     *
     * <p><strong>Образовательный момент:</strong>
     * Кастомные health endpoints дополняют Spring Boot Actuator
     * и позволяют проверить специфичную логику Gateway.
     *
     * @return статус здоровья Gateway
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Gateway is healthy and ready to route requests");
    }

    /**
     * Информационный endpoint для отладки.
     *
     * <p><strong>Образовательный момент:</strong>
     * Информационные endpoints помогают в отладке и мониторинге.
     * Включайте полезную информацию о конфигурации и состоянии.
     *
     * @return информация о Gateway
     */
    @GetMapping("/info")
    public Mono<String> info() {
        return Mono.just("Spring Cloud Gateway Demo - Educational Project\n" +
            "Available routes:\n" +
            "- /demo/** -> httpbin.org\n" +
            "- /programmatic/** -> httpbin.org (programmatic)\n" +
            "- /conditional/** -> httpbin.org (with conditions)\n" +
            "- /custom/** -> httpbin.org (custom predicates)\n" +
            "- /error-test/** -> httpbin.org (with circuit breaker)\n" +
            "- /fallback -> fallback endpoint\n" +
            "- /health -> health check\n" +
            "- /info -> this information");
    }
}