package org.gualsh.demo.gw.config.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Программное создание маршрутов через RouteLocator.
 */
@Slf4j
@Configuration
public class GetewayRouteLocatorConfig {

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
     * Дополнительные программные маршруты для демонстрации расширенных возможностей.
     *
     * <p><strong>Образовательный момент:</strong>
     * Программные маршруты позволяют создавать сложную логику маршрутизации,
     * которая невозможна в конфигурационном файле.
     *
     * @param builder конструктор маршрутов
     * @return настроенный RouteLocator
     */
    @Bean
    public RouteLocator advancedRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Маршрут с кастомной логикой балансировки
            .route("load-balanced-route", r -> r
                .path("/balanced/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter((exchange, chain) -> {
                        // Кастомная логика выбора backend
                        String backend = selectBackend(exchange);
                        exchange.getAttributes().put("backend", backend);
                        return chain.filter(exchange);
                    })
                    .addRequestHeader("X-Selected-Backend", "#{exchange.attributes['backend']}")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с кэшированием
            .route("cached-route", r -> r
                .path("/cached/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addResponseHeader("Cache-Control", "public, max-age=300")
                    .addResponseHeader("X-Cache-Status", "MISS")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с трансформацией данных
            .route("transform-route", r -> r
                .path("/transform/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .modifyRequestBody(String.class, String.class, new RequestTransformer())
                    .modifyResponseBody(String.class, String.class, new ResponseTransformer())
                )
                .uri("https://httpbin.org")
            )

            .build();
    }

    /**
     * Трансформер для модификации тела ответа.
     *
     * <p><strong>Образовательный момент:</strong>
     * Модификация ответа позволяет добавлять дополнительную информацию
     * или трансформировать формат данных для клиентов.
     */
    private static class ResponseTransformer implements RewriteFunction<String, String> {
        @Override
        public org.reactivestreams.Publisher<String> apply(ServerWebExchange exchange, String body) {
            if (body == null || body.isEmpty()) {
                return Mono.just("");
            }

            // Оборачиваем ответ в metadata
            String transformedBody = "{\n" +
                "  \"data\": " + body + ",\n" +
                "  \"metadata\": {\n" +
                "    \"processed_by\": \"gateway\",\n" +
                "    \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                "    \"request_id\": \"" + exchange.getRequest().getHeaders().getFirst("X-Request-ID") + "\",\n" +
                "    \"status\": \"success\"\n" +
                "  }\n" +
                "}";

            return Mono.just(transformedBody);
        }
    }

    /**
     * Трансформер для модификации тела запроса.
     *
     * <p><strong>Образовательный момент:</strong>
     * RewriteFunction позволяет модифицировать тело запроса/ответа.
     * Важно помнить о производительности при работе с большими объемами данных.
     */
    private static class RequestTransformer implements RewriteFunction<String, String> {
        @Override
        public org.reactivestreams.Publisher<String> apply(ServerWebExchange exchange, String body) {
            if (body == null || body.isEmpty()) {
                return Mono.just("");
            }

            // Добавляем метаданные к запросу
            String transformedBody = "{\n" +
                "  \"original_body\": " + body + ",\n" +
                "  \"transformed_by\": \"gateway\",\n" +
                "  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                "  \"request_id\": \"" + exchange.getRequest().getHeaders().getFirst("X-Request-ID") + "\"\n" +
                "}";

            return Mono.just(transformedBody);
        }
    }

    /**
     * Кастомная логика выбора backend сервера.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот метод демонстрирует как можно реализовать кастомную логику
     * балансировки нагрузки на основе различных факторов.
     *
     * @param exchange текущий обмен запросом
     * @return выбранный backend
     */
    private String selectBackend(ServerWebExchange exchange) {
        // Простая логика выбора backend на основе user-agent
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent != null && userAgent.contains("Mobile")) {
            return "mobile-backend";
        } else if (userAgent != null && userAgent.contains("Bot")) {
            return "bot-backend";
        }
        return "default-backend";
    }
}
