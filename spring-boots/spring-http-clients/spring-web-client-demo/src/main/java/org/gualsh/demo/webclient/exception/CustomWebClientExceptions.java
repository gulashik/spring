package org.gualsh.demo.webclient.exception;

import org.springframework.http.HttpStatus;

/**
 * Кастомные исключения для WebClient операций.
 *
 * <p>Используются вместо прямого создания WebClientResponseException,
 * конструкторы которых могут быть непубличными.</p>
 *
 */
public class CustomWebClientExceptions {

    /**
     * Базовое исключение для проблем с внешними API.
     */
    public static class ExternalApiException extends RuntimeException {
        private final HttpStatus status;
        private final String responseBody;

        public ExternalApiException(String message, HttpStatus status) {
            super(message);
            this.status = status;
            this.responseBody = null;
        }

        public ExternalApiException(String message, HttpStatus status, String responseBody) {
            super(message);
            this.status = status;
            this.responseBody = responseBody;
        }

        public ExternalApiException(String message, HttpStatus status, Throwable cause) {
            super(message, cause);
            this.status = status;
            this.responseBody = null;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }

    /**
     * Исключение для превышения лимита запросов (HTTP 429).
     */
    public static class RateLimitExceededException extends ExternalApiException {
        private final long retryAfterSeconds;

        public RateLimitExceededException(String message) {
            super(message, HttpStatus.TOO_MANY_REQUESTS);
            this.retryAfterSeconds = 0;
        }

        public RateLimitExceededException(String message, long retryAfterSeconds) {
            super(message, HttpStatus.TOO_MANY_REQUESTS);
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }

    /**
     * Исключение для ошибок аутентификации (HTTP 401).
     */
    public static class AuthenticationException extends ExternalApiException {
        public AuthenticationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, HttpStatus.UNAUTHORIZED, cause);
        }
    }

    /**
     * Исключение для ошибок авторизации (HTTP 403).
     */
    public static class AuthorizationException extends ExternalApiException {
        public AuthorizationException(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Исключение для не найденных ресурсов (HTTP 404).
     */
    public static class ResourceNotFoundException extends ExternalApiException {
        private final String resourceId;

        public ResourceNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
            this.resourceId = null;
        }

        public ResourceNotFoundException(String message, String resourceId) {
            super(message, HttpStatus.NOT_FOUND);
            this.resourceId = resourceId;
        }

        public String getResourceId() {
            return resourceId;
        }
    }

    /**
     * Исключение для недоступности внешнего сервиса (HTTP 503).
     */
    public static class ServiceUnavailableException extends ExternalApiException {
        public ServiceUnavailableException(String message) {
            super(message, HttpStatus.SERVICE_UNAVAILABLE);
        }

        public ServiceUnavailableException(String message, Throwable cause) {
            super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
        }
    }

    /**
     * Исключение для таймаута gateway (HTTP 504).
     */
    public static class GatewayTimeoutException extends ExternalApiException {
        public GatewayTimeoutException(String message) {
            super(message, HttpStatus.GATEWAY_TIMEOUT);
        }
    }

    /**
     * Исключение для невалидных данных запроса (HTTP 400).
     */
    public static class BadRequestException extends ExternalApiException {
        private final String field;

        public BadRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
            this.field = null;
        }

        public BadRequestException(String message, String field) {
            super(message, HttpStatus.BAD_REQUEST);
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    /**
     * Общее исключение для внутренних ошибок сервера (HTTP 500).
     */
    public static class InternalServerErrorException extends ExternalApiException {
        public InternalServerErrorException(String message) {
            super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        public InternalServerErrorException(String message, Throwable cause) {
            super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
        }
    }
}