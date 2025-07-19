package org.gualsh.demo.gw.config.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Коллекция кастомных фильтров для демонстрации расширенных возможностей Gateway.
 *
 * <p><strong>Образовательный момент:</strong>
 * Кастомные фильтры позволяют реализовать специфичную бизнес-логику:
 * <ul>
 * <li>Аутентификация и авторизация</li>
 * <li>Трансформация данных</li>
 * <li>Мониторинг и метрики</li>
 * <li>Кэширование</li>
 * </ul>
 *
 * @author Spring Cloud Gateway Demo
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class CustomFilters {

    /**
     * Фильтр для добавления кастомных заголовков с информацией о запросе.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот фильтр демонстрирует как создавать параметризованные фильтры,
     * которые могут быть настроены через конфигурацию.
     *
     * <p><strong>Пример использования:</strong>
     * <pre>{@code
     * # В application.yml
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         - id: custom-headers-route
     *           uri: https://httpbin.org
     *           filters:
     *             - name: RequestInfo
     *               args:
     *                 prefix: "X-Custom"
     *                 includeHeaders: true
     * }</pre>
     */
    @Component
    public static class RequestInfoGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RequestInfoGatewayFilterFactory.Config> {

        public RequestInfoGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String prefix = config.getPrefix();

                // Добавляем информацию о запросе в заголовки
                exchange.getRequest().mutate()
                    .header(prefix + "-Method", exchange.getRequest().getMethod().name())
                    .header(prefix + "-Path", exchange.getRequest().getPath().value())
                    .header(prefix + "-Query", exchange.getRequest().getQueryParams().toString())
                    .header(prefix + "-Remote-Address",
                        exchange.getRequest().getRemoteAddress() != null ?
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                            "unknown")
                    .header(prefix + "-Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

                if (config.isIncludeHeaders()) {
                    exchange.getRequest().getHeaders().forEach((key, values) -> {
                        if (!key.toLowerCase().startsWith("x-custom")) {
                            exchange.getRequest().mutate()
                                .header(prefix + "-Original-" + key, String.join(", ", values))
                                .build();
                        }
                    });
                }

                return chain.filter(exchange);
            };
        }

        @Override
        public List<String> shortcutFieldOrder() {
            return Arrays.asList("prefix", "includeHeaders");
        }

        public static class Config {
            private String prefix = "X-Request-Info";
            private boolean includeHeaders = false;

            public String getPrefix() { return prefix; }
            public void setPrefix(String prefix) { this.prefix = prefix; }
            public boolean isIncludeHeaders() { return includeHeaders; }
            public void setIncludeHeaders(boolean includeHeaders) { this.includeHeaders = includeHeaders; }
        }
    }

    /**
     * Фильтр для подсчета метрик запросов.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот фильтр показывает как собирать метрики на уровне Gateway.
     * В production следует использовать Micrometer для интеграции с системами мониторинга.
     */
    @Component
    public static class MetricsGatewayFilterFactory
        extends AbstractGatewayFilterFactory<MetricsGatewayFilterFactory.Config> {

        private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> responseTimes = new ConcurrentHashMap<>();

        public MetricsGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String route = exchange.getAttribute("org.springframework.cloud.gateway.support.RouteDefinitionRouteLocator.ROUTE_DEFINITION");
                String method = exchange.getRequest().getMethod().name();
                String path = exchange.getRequest().getPath().value();
                String key = method + " " + path;

                // Увеличиваем счетчик запросов
                requestCounts.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

                long startTime = System.currentTimeMillis();

                return chain.filter(exchange).doFinally(signal -> {
                    long duration = System.currentTimeMillis() - startTime;
                    responseTimes.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(duration);

                    if (config.isLogMetrics()) {
                        log.info("Request metrics - Route: {}, Method: {}, Path: {}, Duration: {}ms, " +
                                "Total requests: {}",
                            route, method, path, duration, requestCounts.get(key).get());
                    }

                    // Добавляем метрики в response headers
                    exchange.getResponse().getHeaders().add("X-Request-Count",
                        String.valueOf(requestCounts.get(key).get()));
                    exchange.getResponse().getHeaders().add("X-Response-Time",
                        duration + "ms");
                });
            };
        }

        public static class Config {
            private boolean logMetrics = true;

            public boolean isLogMetrics() { return logMetrics; }
            public void setLogMetrics(boolean logMetrics) { this.logMetrics = logMetrics; }
        }
    }

    /**
     * Фильтр для простой аутентификации по API ключу.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот фильтр демонстрирует базовую аутентификацию на уровне Gateway.
     * В production следует использовать OAuth 2.0, JWT или другие стандартные механизмы.
     */
    @Component
    public static class ApiKeyAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<ApiKeyAuthGatewayFilterFactory.Config> {

        private final Map<String, String> validApiKeys = Map.of(
            "demo-key-1", "user1",
            "demo-key-2", "user2",
            "admin-key", "admin"
        );

        public ApiKeyAuthGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                String apiKey = exchange.getRequest().getHeaders().getFirst(config.getHeaderName());

                if (apiKey == null || apiKey.isEmpty()) {
                    return handleUnauthorized(exchange, "API key is required");
                }

                String username = validApiKeys.get(apiKey);
                if (username == null) {
                    return handleUnauthorized(exchange, "Invalid API key");
                }

                // Добавляем информацию о пользователе в заголовки
                exchange.getRequest().mutate()
                    .header("X-Authenticated-User", username)
                    .header("X-Auth-Method", "API-KEY")
                    .build();

                log.info("Authenticated user: {} with API key", username);

                return chain.filter(exchange);
            };
        }

        private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

            String body = "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}";
            org.springframework.core.io.buffer.DataBuffer buffer =
                exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        public static class Config {
            private String headerName = "X-API-Key";

            public String getHeaderName() { return headerName; }
            public void setHeaderName(String headerName) { this.headerName = headerName; }
        }
    }

    /**
     * Фильтр для трансформации JSON запросов.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот фильтр показывает как работать с телом запроса в Gateway.
     * Важно помнить о производительности при работе с большими JSON объектами.
     */
    @Component
    public static class JsonTransformGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JsonTransformGatewayFilterFactory.Config> {

        public JsonTransformGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                        .header("Content-Type", "application/json")
                        .build())
                    .build());
            };
        }

        public static class Config {
            private String transformationType = "wrap";

            public String getTransformationType() { return transformationType; }
            public void setTransformationType(String transformationType) {
                this.transformationType = transformationType;
            }
        }
    }
}


