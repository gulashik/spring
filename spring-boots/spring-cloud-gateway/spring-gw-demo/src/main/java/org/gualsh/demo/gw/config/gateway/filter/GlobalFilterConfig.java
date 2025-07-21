package org.gualsh.demo.gw.config.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
public class GlobalFilterConfig {
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
     * <p>
     * Order(-1) гарантирует выполнение до других фильтров.
     *
     * @return глобальный фильтр для логирования
     */
    @Bean
    @Order(-1) // гарантирует выполнение до других фильтров
    public GlobalFilter loggingGlobalFilter() {
        // Response
        // HttpHeaders headersResponse() = exchange.getResponse().getHeaders();

        // Request
        // HttpHeaders headersRequest = exchange.getRequest().getHeaders();

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
            String path = exchange.getRequest().getPath().value();

            // Блокируем доступ к админским эндпоинтам ДО обработки запроса
            if (path.startsWith("/admin/")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                response.getHeaders().add("Content-Type", "application/json");

                String body = "{\"error\": \"Access denied\"}";
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                return response.writeWith(Mono.just(buffer));
            }

            // Продолжаем обработку и добавляем заголовки в ответ
            return chain.filter(exchange)
                .then(
                    Mono.fromRunnable(
                        () -> {
                            HttpHeaders headers = exchange.getResponse().getHeaders();

                            // Добавляем security заголовки
                            headers.add("X-Content-Type-Options", "nosniff");
                            headers.add("X-Frame-Options", "DENY");
                            headers.add("X-XSS-Protection", "1; mode=block");
                            headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                            headers.add("Content-Security-Policy", "default-src 'self'");
                            headers.add("X-Gateway-Version", "1.0.0");
                            headers.add("GlobalFilter-ModifedBy", "------>HeadersGlobalFilter<------");

                            // Удаляем потенциально опасные заголовки
                            headers.remove("Server");
                            headers.remove("X-Powered-By");
                        }
                    )
                );
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
}