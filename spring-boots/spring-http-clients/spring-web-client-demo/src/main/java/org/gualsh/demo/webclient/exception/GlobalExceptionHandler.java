package org.gualsh.demo.webclient.exception;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.exception.CustomWebClientExceptions.*;
import org.gualsh.demo.webclient.util.HttpStatusUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для reactive WebFlux приложения.
 *
 * <p>Обрабатывает различные типы ошибок:</p>
 * <ul>
 *   <li>WebClient ошибки (4xx, 5xx)</li>
 *   <li>Кастомные исключения External API</li>
 *   <li>Ошибки валидации</li>
 *   <li>Constraint violations</li>
 *   <li>Общие runtime ошибки</li>
 * </ul>
 *
 * <p>Возвращает структурированные JSON ответы с детальной информацией об ошибках.</p>
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки WebClient (HTTP 4xx/5xx).
     *
     * <p>Преобразует WebClientResponseException в структурированный ответ
     * с сохранением оригинального статуса и сообщения.</p>
     *
     * @param ex WebClientResponseException
     * @param exchange ServerWebExchange для получения информации о запросе
     * @return Mono с ResponseEntity содержащим детали ошибки
     */
    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebClientResponseException(
        WebClientResponseException ex, ServerWebExchange exchange) {

        log.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getMessage());

        // Безопасно преобразуем HttpStatusCode в HttpStatus
        HttpStatus httpStatus = HttpStatusUtils.toHttpStatus(ex.getStatusCode());

        Map<String, Object> errorDetails = createErrorResponse(
            httpStatus,
            "External API Error",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        // Добавляем специфичную информацию о WebClient ошибке
        errorDetails.put("statusCode", ex.getStatusCode().value());
        errorDetails.put("statusText", HttpStatusUtils.getReasonPhrase(ex.getStatusCode()));
        errorDetails.put("displayMessage", HttpStatusUtils.getDisplayMessage(ex.getStatusCode()));
        errorDetails.put("shouldRetry", HttpStatusUtils.shouldRetry(ex.getStatusCode()));

        // Безопасно добавляем response body если он есть
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                errorDetails.put("responseBody", responseBody);
            }
        } catch (Exception e) {
            log.debug("Could not read response body: {}", e.getMessage());
        }

        return Mono.just(ResponseEntity.status(httpStatus).body(errorDetails));
    }

    /**
     * Обрабатывает кастомные исключения External API.
     *
     * @param ex ExternalApiException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(ExternalApiException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleExternalApiException(
        ExternalApiException ex, ServerWebExchange exchange) {

        log.error("External API error: {} - {}", ex.getStatus(), ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            ex.getStatus(),
            "External API Error",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        if (ex.getResponseBody() != null) {
            errorDetails.put("responseBody", ex.getResponseBody());
        }

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(errorDetails));
    }

    /**
     * Обрабатывает исключения превышения лимита запросов.
     *
     * @param ex RateLimitExceededException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRateLimitExceeded(
        RateLimitExceededException ex, ServerWebExchange exchange) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate Limit Exceeded",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        if (ex.getRetryAfterSeconds() > 0) {
            errorDetails.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        if (ex.getRetryAfterSeconds() > 0) {
            responseBuilder.header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        }

        return Mono.just(responseBuilder.body(errorDetails));
    }

    /**
     * Обрабатывает исключения не найденных ресурсов.
     *
     * @param ex ResourceNotFoundException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResourceNotFound(
        ResourceNotFoundException ex, ServerWebExchange exchange) {

        log.warn("Resource not found: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        if (ex.getResourceId() != null) {
            errorDetails.put("resourceId", ex.getResourceId());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails));
    }

    /**
     * Обрабатывает исключения аутентификации.
     *
     * @param ex AuthenticationException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAuthentication(
        AuthenticationException ex, ServerWebExchange exchange) {

        log.warn("Authentication error: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Authentication Failed",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails));
    }

    /**
     * Обрабатывает исключения авторизации.
     *
     * @param ex AuthorizationException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(AuthorizationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleAuthorization(
        AuthorizationException ex, ServerWebExchange exchange) {

        log.warn("Authorization error: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "Authorization Failed",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDetails));
    }

    /**
     * Обрабатывает исключения недоступности сервиса.
     *
     * @param ex ServiceUnavailableException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleServiceUnavailable(
        ServiceUnavailableException ex, ServerWebExchange exchange) {

        log.error("Service unavailable: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        // Добавляем рекомендации по retry
        errorDetails.put("shouldRetry", true);
        errorDetails.put("recommendedRetryDelay", "30 seconds");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorDetails));
    }

    /**
     * Обрабатывает ошибки валидации для reactive endpoints.
     *
     * <p>Собирает все ошибки валидации полей и возвращает детальную информацию.</p>
     *
     * @param ex WebExchangeBindException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity содержащим ошибки валидации
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
        WebExchangeBindException ex, ServerWebExchange exchange) {

        log.warn("Validation error for path: {}", exchange.getRequest().getPath().value());

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null ?
                    fieldError.getDefaultMessage() : "Validation failed",
                (existing, replacement) -> existing + "; " + replacement
            ));

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Request validation failed for one or more fields",
            exchange.getRequest().getPath().value()
        );

        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("errorCount", fieldErrors.size());

        return Mono.just(ResponseEntity.badRequest().body(errorDetails));
    }

    /**
     * Обрабатывает ошибки валидации для non-reactive endpoints.
     *
     * @param ex MethodArgumentNotValidException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity содержащим ошибки валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, ServerWebExchange exchange) {

        log.warn("Method argument validation error for path: {}", exchange.getRequest().getPath().value());

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null ?
                    fieldError.getDefaultMessage() : "Validation failed"
            ));

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            "Method argument validation failed",
            exchange.getRequest().getPath().value()
        );

        errorDetails.put("fieldErrors", fieldErrors);

        return Mono.just(ResponseEntity.badRequest().body(errorDetails));
    }

    /**
     * Обрабатывает ошибки constraint validation.
     *
     * @param ex ConstraintViolationException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity содержащим ошибки валидации
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleConstraintViolationException(
        ConstraintViolationException ex, ServerWebExchange exchange) {

        log.warn("Constraint violation error for path: {}", exchange.getRequest().getPath().value());

        Map<String, String> violations = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Constraint Violation",
            "Request parameters violate constraints",
            exchange.getRequest().getPath().value()
        );

        errorDetails.put("violations", violations);

        return Mono.just(ResponseEntity.badRequest().body(errorDetails));
    }

    /**
     * Обрабатывает общие runtime ошибки.
     *
     * @param ex RuntimeException
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity содержащим детали ошибки
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(
        RuntimeException ex, ServerWebExchange exchange) {

        log.error("Runtime error for path: {} - {}", exchange.getRequest().getPath().value(), ex.getMessage(), ex);

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            ex.getMessage(),
            exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails));
    }

    /**
     * Обрабатывает все остальные исключения.
     *
     * @param ex Exception
     * @param exchange ServerWebExchange
     * @return Mono с ResponseEntity содержащим общую ошибку
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
        Exception ex, ServerWebExchange exchange) {

        log.error("Unexpected error for path: {} - {}", exchange.getRequest().getPath().value(), ex.getMessage(), ex);

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected Error",
            "An unexpected error occurred",
            exchange.getRequest().getPath().value()
        );

        // В production не показываем детали внутренних ошибок
        if (log.isDebugEnabled()) {
            errorDetails.put("debugMessage", ex.getMessage());
            errorDetails.put("exceptionType", ex.getClass().getSimpleName());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails));
    }

    /**
     * Создает стандартизированную структуру ответа об ошибке.
     *
     * @param status HTTP статус
     * @param error тип ошибки
     * @param message сообщение об ошибке
     * @param path путь запроса где произошла ошибка
     * @return Map с деталями ошибки
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message != null ? message : "No message available");
        errorResponse.put("path", path);
        return errorResponse;
    }
}