package org.gualsh.demo.webclient.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Руководство по правильной обработке исключений WebClient.
 *
 * <p>В Spring WebFlux WebClientResponseException имеет специфичные подклассы
 * для различных HTTP статус кодов, а не общие ServerError/ClientError.</p>
 *
 * @author Demo
 * @version 1.0
 */
public class WebClientExceptionGuide {

    /**
     * Правильные классы исключений WebClient для 4xx ошибок (Client Errors).
     *
     * <p>Используйте эти классы в @Retryable или catch блоках:</p>
     * <ul>
     *   <li>{@link WebClientResponseException.BadRequest} - HTTP 400</li>
     *   <li>{@link WebClientResponseException.Unauthorized} - HTTP 401</li>
     *   <li>{@link WebClientResponseException.Forbidden} - HTTP 403</li>
     *   <li>{@link WebClientResponseException.NotFound} - HTTP 404</li>
     *   <li>{@link WebClientResponseException.MethodNotAllowed} - HTTP 405</li>
     *   <li>{@link WebClientResponseException.NotAcceptable} - HTTP 406</li>
     *   <li>{@link WebClientResponseException.Conflict} - HTTP 409</li>
     *   <li>{@link WebClientResponseException.Gone} - HTTP 410</li>
     *   <li>{@link WebClientResponseException.UnsupportedMediaType} - HTTP 415</li>
     *   <li>{@link WebClientResponseException.UnprocessableEntity} - HTTP 422</li>
     *   <li>{@link WebClientResponseException.TooManyRequests} - HTTP 429</li>
     * </ul>
     */
    public void handleClientErrors() {
        // ✅ Правильно: специфичные исключения
        // @Retryable(value = {WebClientResponseException.TooManyRequests.class})

        // ❌ Неправильно: WebClientResponseException.ClientError не существует
        // @Retryable(value = {WebClientResponseException.ClientError.class})
    }

    /**
     * Правильные классы исключений WebClient для 5xx ошибок (Server Errors).
     *
     * <p>Используйте эти классы для retry логики:</p>
     * <ul>
     *   <li>{@link WebClientResponseException.InternalServerError} - HTTP 500</li>
     *   <li>{@link WebClientResponseException.NotImplemented} - HTTP 501</li>
     *   <li>{@link WebClientResponseException.BadGateway} - HTTP 502</li>
     *   <li>{@link WebClientResponseException.ServiceUnavailable} - HTTP 503</li>
     *   <li>{@link WebClientResponseException.GatewayTimeout} - HTTP 504</li>
     * </ul>
     */
    public void handleServerErrors() {
        // ✅ Правильно: конкретные серверные ошибки для retry
        /*
        @Retryable(value = {
            WebClientResponseException.InternalServerError.class,
            WebClientResponseException.BadGateway.class,
            WebClientResponseException.ServiceUnavailable.class,
            WebClientResponseException.GatewayTimeout.class
        })
        */

        // ❌ Неправильно: WebClientResponseException.ServerError не существует
        // @Retryable(value = {WebClientResponseException.ServerError.class})
    }

    /**
     * Универсальный подход для проверки типа ошибки.
     *
     * <p>Используйте методы HttpStatus для проверки категории ошибки:</p>
     */
    public void universalErrorHandling() {
        /*
        // ✅ Правильно: проверка категории через HttpStatus
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
            .filter(throwable -> {
                if (throwable instanceof WebClientResponseException) {
                    WebClientResponseException ex = (WebClientResponseException) throwable;
                    return ex.getStatusCode().is5xxServerError(); // или is4xxClientError()
                }
                return false;
            }))
        */
    }

    /**
     * Рекомендуемые стратегии retry для различных типов ошибок.
     */
    public void retryStrategies() {
        /*
        // ✅ Retry только для серверных ошибок и rate limiting
        @Retryable(
            value = {
                WebClientResponseException.InternalServerError.class,  // 500
                WebClientResponseException.BadGateway.class,           // 502
                WebClientResponseException.ServiceUnavailable.class,  // 503
                WebClientResponseException.GatewayTimeout.class,      // 504
                WebClientResponseException.TooManyRequests.class      // 429
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
        )

        // ❌ НЕ делайте retry для клиентских ошибок:
        // - BadRequest (400) - данные невалидны
        // - Unauthorized (401) - проблемы с аутентификацией
        // - Forbidden (403) - недостаточно прав
        // - NotFound (404) - ресурс не существует
        */
    }

    /**
     * Обработка специфичных ошибок с разной логикой.
     */
    public void specificErrorHandling() {
        /*
        webClient.get()
            .uri("/api/resource")
            .retrieve()
            .onStatus(HttpStatus.UNAUTHORIZED::equals,
                response -> Mono.error(new AuthenticationException("Invalid credentials")))
            .onStatus(HttpStatus.FORBIDDEN::equals,
                response -> Mono.error(new AuthorizationException("Access denied")))
            .onStatus(HttpStatus.NOT_FOUND::equals,
                response -> Mono.error(new ResourceNotFoundException("Resource not found")))
            .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals,
                response -> Mono.error(new RateLimitException("Rate limit exceeded")))
            .onStatus(HttpStatus::is5xxServerError,
                response -> Mono.error(new ExternalServiceException("Service unavailable")))
            .bodyToMono(String.class);
        */
    }

    /**
     * Обработка таймаутов и сетевых ошибок.
     *
     * <p>Эти ошибки не являются WebClientResponseException,
     * так как не получен HTTP ответ:</p>
     */
    public void networkErrorHandling() {
        /*
        // Для сетевых ошибок используйте:
        @Retryable(
            value = {
                java.net.ConnectException.class,           // Не удалось подключиться
                java.util.concurrent.TimeoutException.class,  // Таймаут
                java.nio.channels.ClosedChannelException.class, // Канал закрыт
                io.netty.channel.ConnectTimeoutException.class  // Netty таймаут подключения
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
        )
        */
    }

    /**
     * Комплексная стратегия обработки всех типов ошибок.
     */
    public void comprehensiveErrorHandling() {
        /*
        return webClient.get()
            .uri("/api/endpoint")
            .retrieve()
            .bodyToMono(String.class)
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(delay))
                .filter(throwable -> {
                    // Retry для серверных ошибок HTTP
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        return ex.getStatusCode().is5xxServerError() ||
                               ex.getStatusCode().value() == 429; // Too Many Requests
                    }
                    // Retry для сетевых ошибок
                    return throwable instanceof ConnectException ||
                           throwable instanceof TimeoutException ||
                           throwable instanceof ClosedChannelException;
                })
                .onRetryExhaustedThrow((spec, signal) ->
                    new RuntimeException("Все попытки исчерпаны после " + signal.totalRetries() + " попыток")))
            .doOnError(WebClientResponseException.Unauthorized.class,
                ex -> log.warn("Ошибка аутентификации: {}", ex.getMessage()))
            .doOnError(WebClientResponseException.NotFound.class,
                ex -> log.warn("Ресурс не найден: {}", ex.getMessage()))
            .onErrorReturn(WebClientResponseException.ServiceUnavailable.class, "Service temporarily unavailable");
        */
    }
}