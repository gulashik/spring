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
 */
@SpringBootApplication
@RestController
public class GatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }

    /**
     * Программное создание маршрутов через RouteLocator.
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
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с модификацией тела запроса (ПРОГРАММНАЯ НАСТРОЙКА)
            .route("modify-request-route", r -> r
                .path("/modify-request/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .modifyRequestBody(String.class, String.class,
                        (exchange, body) -> {
                            if (body == null || body.trim().isEmpty()) {
                                body = "{}";
                            }
                            String modifiedBody = "{\n" +
                                "  \"originalRequest\": " + body + ",\n" +
                                "  \"metadata\": {\n" +
                                "    \"modifiedBy\": \"Gateway\",\n" +
                                "    \"timestamp\": \"" + java.time.LocalDateTime.now() + "\"\n" +
                                "  }\n" +
                                "}";
                            return Mono.just(modifiedBody);
                        })
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с модификацией тела ответа (ПРОГРАММНАЯ НАСТРОЙКА)
            .route("modify-response-route", r -> r
                .path("/modify-response/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .modifyResponseBody(String.class, String.class,
                        (exchange, body) -> {
                            if (body == null || body.trim().isEmpty()) {
                                body = "{}";
                            }
                            String modifiedBody = "{\n" +
                                "  \"success\": true,\n" +
                                "  \"data\": " + body + ",\n" +
                                "  \"metadata\": {\n" +
                                "    \"processedBy\": \"Gateway\",\n" +
                                "    \"timestamp\": \"" + java.time.LocalDateTime.now() + "\"\n" +
                                "  }\n" +
                                "}";
                            return Mono.just(modifiedBody);
                        })
                )
                .uri("https://httpbin.org")
            )

            .build();
    }

    /**
     * Fallback endpoint для Circuit Breaker.
     */
    @GetMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("Service temporarily unavailable. Please try again later.");
    }

    /**
     * Health check endpoint для мониторинга.
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("Gateway is healthy and ready to route requests");
    }

    /**
     * Информационный endpoint для отладки.
     */
    @GetMapping("/info")
    public Mono<String> info() {
        return Mono.just("Spring Cloud Gateway Demo - Educational Project\n" +
            "Available routes:\n" +
            "- /demo/** -> httpbin.org\n" +
            "- /programmatic/** -> httpbin.org (programmatic)\n" +
            "- /conditional/** -> httpbin.org (with conditions)\n" +
            "- /modify-request/** -> httpbin.org (request body modification)\n" +
            "- /modify-response/** -> httpbin.org (response body modification)\n" +
            "- /circuit-breaker/** -> httpbin.org (with circuit breaker)\n" +
            "- /retry/** -> httpbin.org (with retry)\n" +
            "- /fallback -> fallback endpoint\n" +
            "- /health -> health check\n" +
            "- /info -> this information");
    }
}