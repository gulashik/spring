package org.gualsh.demo.gw.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конфигурация Spring Cloud Gateway для демонстрации расширенных возможностей.
 *
 * <p><strong>Образовательный момент:</strong>
 * Этот класс демонстрирует различные способы конфигурации Gateway:
 * <ul>
 * <li>Глобальные фильтры для всех маршрутов</li>
 * <li>Кастомные фильтры для специфичной логики</li>
 * <li>Интеграция с external системами</li>
 * <li>Обработка ошибок и метрики</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * // Глобальные фильтры автоматически применяются ко всем запросам
 * curl -X GET http://localhost:8080/demo/get -H "X-Correlation-ID: test-123"
 *
 * // Проверка добавленных заголовков
 * curl -X GET http://localhost:8080/demo/headers
 * }</pre>
 *
 * @author Spring Cloud Gateway Demo
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * Глобальный фильтр для логирования запросов.
     *
     * <p><strong>Образовательный момент:</strong>
     * Глобальные фильтры выполняются для всех маршрутов и являются
     * идеальным местом для:
     * <ul>
     * <li>Логирования и аудита</li>
     * <li>Добавления общих заголовков</li>
     * <li>Аутентификации и авторизации</li>
     * <li>Сбора метрик</li>
     * </ul>
     *
     * Order(-1) гарантирует выполнение до других фильтров.
     *
     * @return глобальный фильтр для логирования
     */
    @Bean
    @Order(-1)
    public GlobalFilter loggingGlobalFilter() {
        return (exchange, chain) -> {
            String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
            if (requestId == null) {
                requestId = java.util.UUID.randomUUID().toString();
            }

            // Создаем final переменные для использования в lambda
            final String finalRequestId = requestId;
            final String method = exchange.getRequest().getMethod().name();
            final String path = exchange.getRequest().getPath().value();
            final String remoteAddress = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

            log.info("Request started: {} {} from {} with ID: {}",
                method, path, remoteAddress, finalRequestId);

            // Добавляем request ID в заголовки для трассировки
            ServerWebExchange modifiedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .header("X-Request-ID", finalRequestId)
                    .header("X-Gateway-Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build())
                .build();

            long startTime = System.currentTimeMillis();

            return chain.filter(modifiedExchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = modifiedExchange.getResponse().getStatusCode() != null ?
                        modifiedExchange.getResponse().getStatusCode().value() : 200;
                    log.info("Request completed: {} {} -> {} in {}ms with ID: {}",
                        method, path, statusCode, duration, finalRequestId);
                })
                .doOnError(error -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.error("Request failed: {} {} in {}ms with ID: {} - Error: {}",
                        method, path, duration, finalRequestId, error.getMessage());
                });
        };
    }

    /**
     * Глобальный фильтр для добавления security заголовков.
     *
     * <p><strong>Образовательный момент:</strong>
     * Security заголовки критически важны для безопасности API Gateway.
     * Этот фильтр демонстрирует добавление стандартных security заголовков.
     *
     * @return глобальный фильтр для security заголовков
     */
    @Bean
    @Order(0)
    public GlobalFilter securityHeadersGlobalFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();

                // Добавляем security заголовки
                headers.add("X-Content-Type-Options", "nosniff");
                headers.add("X-Frame-Options", "DENY");
                headers.add("X-XSS-Protection", "1; mode=block");
                headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                headers.add("Content-Security-Policy", "default-src 'self'");
                headers.add("X-Gateway-Version", "1.0.0");

                // Удаляем потенциально опасные заголовки
                headers.remove("Server");
                headers.remove("X-Powered-By");
            }));
        };
    }

    /**
     * Глобальный фильтр для обработки CORS.
     *
     * <p><strong>Образовательный момент:</strong>
     * CORS должен обрабатываться на уровне Gateway для единообразия.
     * Этот фильтр показывает кастомную обработку CORS запросов.
     *
     * @return глобальный фильтр для CORS
     */
    @Bean
    @Order(1)
    public GlobalFilter corsGlobalFilter() {
        return (exchange, chain) -> {
            ServerWebExchange.Builder builder = exchange.mutate();

            // Обработка preflight запросов
            if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
                headers.add("Access-Control-Max-Age", "86400");

                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                HttpHeaders headers = exchange.getResponse().getHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Expose-Headers", "X-Request-ID, X-Gateway-Timestamp");
            }));
        };
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
}