package org.gualsh.demo.gw.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик ошибок для Spring Cloud Gateway.
 *
 * Глобальный обработчик ошибок критически важен для Gateway, так как:
 * <ul>
 * <li>Обеспечивает единообразный формат ошибок</li>
 * <li>Скрывает внутренние детали от клиентов</li>
 * <li>Позволяет логировать ошибки для мониторинга</li>
 * <li>Предоставляет понятную информацию разработчикам</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * // Тестирование обработки ошибок
 * curl -X GET http://localhost:8080/nonexistent-route
 * curl -X GET http://localhost:8080/demo/status/500
 * curl -X GET http://localhost:8080/circuit-breaker/delay/10
 * }</pre>
 *
 * <p><strong>Важные аспекты:</strong>
 * <ul>
 * <li>Order(-1) для выполнения до других обработчиков</li>
 * <li>Reactive подход с использованием Mono</li>
 * <li>Правильная обработка различных типов ошибок</li>
 * <li>Безопасность - не раскрывать внутренние детали</li>
 * </ul>

 */
@Slf4j
@Configuration
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * Конструктор с настройкой ObjectMapper.
     *
     * <p><strong>Образовательный момент:</strong>
     * ObjectMapper должен быть правильно настроен для сериализации
     * всех типов данных, включая Java Time API.
     */
    public GlobalErrorHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Обрабатывает все необработанные исключения в Gateway.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот метод является центральной точкой обработки ошибок.
     * Важно правильно категоризировать ошибки и предоставлять
     * соответствующие HTTP статусы и сообщения.
     *
     * @param exchange текущий обмен запросом/ответом
     * @param ex исключение для обработки
     * @return Mono<Void> для асинхронной обработки
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Определяем тип ошибки и соответствующий HTTP статус
        ErrorInfo errorInfo = determineErrorInfo(ex);

        // Логируем ошибку с контекстом
        logError(exchange, ex, errorInfo);

        // Устанавливаем HTTP статус
        exchange.getResponse().setStatusCode(errorInfo.getHttpStatus());

        // Устанавливаем заголовки ответа
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        exchange.getResponse().getHeaders().add("X-Error-Handler", "GlobalErrorHandler");

        // Создаем тело ответа с информацией об ошибке
        String errorBody = createErrorResponse(exchange, errorInfo);

        // Записываем ответ
        DataBuffer buffer = exchange.getResponse().bufferFactory()
            .wrap(errorBody.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Определяет тип ошибки и соответствующую информацию.
     *
     * <p><strong>Образовательный момент:</strong>
     * Правильная категоризация ошибок помогает:
     * <ul>
     * <li>Предоставить понятные сообщения клиентам</li>
     * <li>Выбрать правильный HTTP статус</li>
     * <li>Определить уровень логирования</li>
     * <li>Принять решение о retry логике</li>
     * </ul>
     *
     * @param ex исключение для анализа
     * @return информация об ошибке
     */
    private ErrorInfo determineErrorInfo(Throwable ex) {
        // Обработка различных типов исключений
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return new ErrorInfo(
                HttpStatus.valueOf(rse.getStatusCode().value()),
                rse.getReason() != null ? rse.getReason() : "Request failed",
                "CLIENT_ERROR",
                rse.getMessage()
            );
        }

        // Обработка ошибок соединения
        if (ex instanceof java.net.ConnectException) {
            return new ErrorInfo(
                HttpStatus.BAD_GATEWAY,
                "Service temporarily unavailable",
                "CONNECTION_ERROR",
                "Failed to connect to upstream service"
            );
        }

        // Обработка timeout ошибок
        if (ex instanceof java.util.concurrent.TimeoutException ||
            ex.getMessage().contains("timeout")) {
            return new ErrorInfo(
                HttpStatus.GATEWAY_TIMEOUT,
                "Request timeout",
                "TIMEOUT_ERROR",
                "Request processing took too long"
            );
        }

        // Обработка Circuit Breaker ошибок
        if (ex.getClass().getSimpleName().contains("CircuitBreaker") ||
            ex.getMessage().contains("circuit breaker")) {
            return new ErrorInfo(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable",
                "CIRCUIT_BREAKER_OPEN",
                "Circuit breaker is open"
            );
        }

        // Обработка Rate Limiting ошибок
        if (ex.getMessage().contains("rate limit") ||
            ex.getMessage().contains("too many requests")) {
            return new ErrorInfo(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded",
                "RATE_LIMIT_EXCEEDED",
                "Too many requests, please try again later"
            );
        }

        // Обработка SSL/TLS ошибок
        if (ex instanceof javax.net.ssl.SSLException) {
            return new ErrorInfo(
                HttpStatus.BAD_GATEWAY,
                "SSL connection failed",
                "SSL_ERROR",
                "Failed to establish secure connection"
            );
        }

        // Обработка ошибок маршрутизации
        if (ex.getMessage().contains("No route") ||
            ex.getMessage().contains("404") ||
            ex.getMessage().contains("Not Found")) {
            return new ErrorInfo(
                HttpStatus.NOT_FOUND,
                "Route not found",
                "ROUTE_NOT_FOUND",
                "No matching route found for the request"
            );
        }

        // Обработка ошибок валидации
        if (ex instanceof IllegalArgumentException) {
            return new ErrorInfo(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                "VALIDATION_ERROR",
                ex.getMessage()
            );
        }

        // Общая обработка для всех остальных ошибок
        return new ErrorInfo(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "INTERNAL_ERROR",
            "An unexpected error occurred"
        );
    }

    /**
     * Логирует ошибку с соответствующим контекстом.
     *
     * <p><strong>Образовательный момент:</strong>
     * Качественное логирование ошибок критически важно для:
     * <ul>
     * <li>Диагностики проблем в production</li>
     * <li>Мониторинга здоровья системы</li>
     * <li>Анализа трендов ошибок</li>
     * <li>Настройки alerting</li>
     * </ul>
     *
     * @param exchange текущий обмен
     * @param ex исключение
     * @param errorInfo информация об ошибке
     */
    private void logError(ServerWebExchange exchange, Throwable ex, ErrorInfo errorInfo) {
        String method = exchange.getRequest().getMethod() != null ?
            exchange.getRequest().getMethod().name() : "UNKNOWN";
        String path = exchange.getRequest().getPath().value();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null ?
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");

        // Выбираем уровень логирования на основе типа ошибки
        switch (errorInfo.getHttpStatus().series()) {
            case CLIENT_ERROR:
                log.warn("Client error: {} {} from {} - Status: {}, Type: {}, Message: {}, RequestId: {}, UserAgent: {}",
                    method, path, remoteAddress, errorInfo.getHttpStatus().value(),
                    errorInfo.getErrorType(), errorInfo.getMessage(), requestId, userAgent);
                break;
            case SERVER_ERROR:
                log.error("Server error: {} {} from {} - Status: {}, Type: {}, RequestId: {}, UserAgent: {}",
                    method, path, remoteAddress, errorInfo.getHttpStatus().value(),
                    errorInfo.getErrorType(), requestId, userAgent, ex);
                break;
            default:
                log.info("Request error: {} {} from {} - Status: {}, Type: {}, RequestId: {}",
                    method, path, remoteAddress, errorInfo.getHttpStatus().value(),
                    errorInfo.getErrorType(), requestId);
        }
    }

    /**
     * Создает JSON ответ с информацией об ошибке.
     *
     * <p><strong>Образовательный момент:</strong>
     * Структурированный ответ об ошибке должен содержать:
     * <ul>
     * <li>Понятное сообщение для пользователя</li>
     * <li>Код ошибки для программной обработки</li>
     * <li>Timestamp для корреляции с логами</li>
     * <li>Request ID для трассировки</li>
     * <li>Информацию для разработчиков (в dev режиме)</li>
     * </ul>
     *
     * @param exchange текущий обмен
     * @param errorInfo информация об ошибке
     * @return JSON строка с информацией об ошибке
     */
    private String createErrorResponse(ServerWebExchange exchange, ErrorInfo errorInfo) {
        Map<String, Object> errorResponse = new HashMap<>();

        // Основная информация об ошибке
        errorResponse.put("error", true);
        errorResponse.put("status", errorInfo.getHttpStatus().value());
        errorResponse.put("error_code", errorInfo.getErrorType());
        errorResponse.put("message", errorInfo.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Информация о запросе
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("method", exchange.getRequest().getMethod() != null ?
            exchange.getRequest().getMethod().name() : "UNKNOWN");

        // Request ID для трассировки
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId != null) {
            errorResponse.put("request_id", requestId);
        }

        // Дополнительная информация для разработчиков
        Map<String, Object> details = new HashMap<>();
        details.put("error_type", errorInfo.getErrorType());
        details.put("gateway_version", "1.0.0");
        details.put("user_agent", exchange.getRequest().getHeaders().getFirst("User-Agent"));

        // В development режиме добавляем техническую информацию
        String profile = System.getProperty("spring.profiles.active", "default");
        if ("development".equals(profile) || "dev".equals(profile)) {
            details.put("internal_message", errorInfo.getInternalMessage());
            details.put("headers", exchange.getRequest().getHeaders().toSingleValueMap());
        }

        errorResponse.put("details", details);

        // Конвертируем в JSON
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            // Fallback на простой JSON
            return "{\"error\":true,\"message\":\"Internal server error\",\"status\":500}";
        }
    }

    /**
     * Внутренний класс для хранения информации об ошибке.
     *
     * <p><strong>Образовательный момент:</strong>
     * Использование внутренних классов для группировки связанных данных
     * улучшает читаемость и поддерживаемость кода.
     */
    private static class ErrorInfo {
        private final HttpStatus httpStatus;
        private final String message;
        private final String errorType;
        private final String internalMessage;

        public ErrorInfo(HttpStatus httpStatus, String message, String errorType, String internalMessage) {
            this.httpStatus = httpStatus;
            this.message = message;
            this.errorType = errorType;
            this.internalMessage = internalMessage;
        }

        public HttpStatus getHttpStatus() { return httpStatus; }
        public String getMessage() { return message; }
        public String getErrorType() { return errorType; }
        public String getInternalMessage() { return internalMessage; }
    }
}

