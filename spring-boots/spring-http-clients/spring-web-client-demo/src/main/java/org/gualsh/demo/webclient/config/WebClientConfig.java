package org.gualsh.demo.webclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.exception.CustomWebClientExceptions.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Конфигурация WebClient с настройками производительности, безопасности и мониторинга.
 *
 * <p>Включает следующие настройки:</p>
 * <ul>
 *   <li>Connection pooling для эффективного использования соединений</li>
 *   <li>Таймауты для предотвращения зависших запросов</li>
 *   <li>Фильтры для логирования и обработки ошибок</li>
 *   <li>Кодеки для сериализации JSON</li>
 *   <li>Retry механизм на уровне WebClient</li>
 * </ul>
 *
 * @see WebClient
 * @see ConnectionProvider
 * @see HttpClient
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${webclient.max-memory-size:2MB}")
    private String maxMemorySize;

    @Value("${webclient.connection-pool.max-connections:100}")
    private int maxConnections;

    @Value("${webclient.connection-pool.pending-acquire-timeout:45s}")
    private Duration pendingAcquireTimeout;

    @Value("${webclient.connection-pool.max-idle-time:20s}")
    private Duration maxIdleTime;

    @Value("${webclient.connection-pool.max-life-time:60s}")
    private Duration maxLifeTime;

    /**
     * Создает основной WebClient bean с оптимизированными настройками.
     *
     * <p>Настройки включают:</p>
     * <ul>
     *   <li>Connection pooling с ограничениями</li>
     *   <li>Таймауты чтения и записи</li>
     *   <li>Увеличенный размер буфера памяти</li>
     *   <li>Фильтры для логирования и обработки ошибок</li>
     * </ul>
     *
     * @return настроенный экземпляр WebClient
     */
    @Bean
    public WebClient webClient() {
        log.info("Creating WebClient with connection pool max connections: {}", maxConnections);

        // Настройка connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
            .maxConnections(maxConnections)
            .pendingAcquireTimeout(pendingAcquireTimeout)
            .maxIdleTime(maxIdleTime)
            .maxLifeTime(maxLifeTime)
            .build();

        // Настройка HTTP клиента с таймаутами
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Connection timeout
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS)) // Read timeout
                    .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))) // Write timeout
            .compress(true);

        // Настройка стратегий обмена с увеличенным размером буфера
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> {
                configurer.defaultCodecs().maxInMemorySize(parseSize(maxMemorySize));
                // Настройка Jackson кодеков
                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
            })
            .build();

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "WebClient-Demo/1.0")
            .filter(loggingFilter())
            .filter(errorHandlingFilter())
            .build();
    }

    /**
     * Создает WebClient для JSONPlaceholder API.
     *
     * @param jsonPlaceholderBaseUrl базовый URL для JSONPlaceholder
     * @return WebClient с предустановленным base URL
     */
    @Bean
    public WebClient jsonPlaceholderWebClient(
        WebClient webClient,
        @Value("${external-api.jsonplaceholder.base-url}") String jsonPlaceholderBaseUrl) {

        log.info("Creating JSONPlaceholder WebClient with base URL: {}", jsonPlaceholderBaseUrl);

        return webClient
            .mutate() // копируем и создаём новый
            .baseUrl(jsonPlaceholderBaseUrl)
            .build();
    }

    /**
     * Создает WebClient для Weather API.
     *
     * @param weatherBaseUrl базовый URL для Weather API
     * @param weatherApiKey API ключ для Weather сервиса
     * @return WebClient с предустановленными настройками для Weather API
     */
    @Bean
    public WebClient weatherWebClient(
        WebClient webClient,
        @Value("${external-api.weather.base-url}") String weatherBaseUrl,
        @Value("${external-api.weather.api-key}") String weatherApiKey) {

        log.info("Creating Weather WebClient with base URL: {}", weatherBaseUrl);

        return webClient
            .mutate() // копируем и создаём новый
            .baseUrl(weatherBaseUrl)
            .defaultHeader("X-API-Key", weatherApiKey)
            .filter(rateLimitingFilter())
            .build();
    }

    /**
     * Фильтр для логирования запросов и ответов.
     *
     * <p>Логирует:</p>
     * <ul>
     *   <li>HTTP метод и URL запроса</li>
     *   <li>Заголовки запроса (исключая чувствительные данные)</li>
     *   <li>Статус код и время выполнения ответа</li>
     *   <li>Ошибки при выполнении запроса</li>
     * </ul>
     *
     * @return фильтр для логирования
     */
    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> {
                if (!isSensitiveHeader(name)) {
                    log.debug("Request Header: {}={}", name, values);
                }
            });
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response Status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        }));
    }

    /**
     * Улучшенный фильтр для обработки ошибок с кастомными исключениями.
     *
     * <p>Преобразует HTTP ошибки в понятные бизнес-исключения:</p>
     * <ul>
     *   <li>400 → BadRequestException</li>
     *   <li>401 → AuthenticationException</li>
     *   <li>403 → AuthorizationException</li>
     *   <li>404 → ResourceNotFoundException</li>
     *   <li>429 → RateLimitExceededException</li>
     *   <li>5xx → соответствующие серверные исключения</li>
     * </ul>
     *
     * @return фильтр для обработки ошибок
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            org.springframework.http.HttpStatusCode statusCode = clientResponse.statusCode();

            if (statusCode.is4xxClientError()) {
                return handleClientError(clientResponse, statusCode);
            } else if (statusCode.is5xxServerError()) {
                return handleServerError(clientResponse, statusCode);
            }

            return Mono.just(clientResponse);
        });
    }

    /**
     * Обрабатывает клиентские ошибки (4xx).
     */
    private Mono<ClientResponse> handleClientError(
        ClientResponse clientResponse,
        HttpStatusCode statusCode) {

        log.warn("Client error: {} for request", statusCode);

        return clientResponse.bodyToMono(String.class)
            .defaultIfEmpty("")
            .flatMap(body -> {
                log.warn("Error response body: {}", body);

                // Создаем соответствующее исключение в зависимости от статуса
                if (statusCode.value() == 400) {
                    return Mono.error(new BadRequestException("Bad request: " + body));
                } else if (statusCode.value() == 401) {
                    return Mono.error(new AuthenticationException("Authentication failed: " + body));
                } else if (statusCode.value() == 403) {
                    return Mono.error(new AuthorizationException("Access forbidden: " + body));
                } else if (statusCode.value() == 404) {
                    return Mono.error(new ResourceNotFoundException("Resource not found: " + body));
                } else if (statusCode.value() == 429) {
                    return Mono.error(new RateLimitExceededException("Rate limit exceeded: " + body));
                } else {
                    // Общая клиентская ошибка
                    return Mono.error(new BadRequestException("Client error " + statusCode + ": " + body));
                }
            });
    }

    /**
     * Обрабатывает серверные ошибки (5xx).
     */
    private Mono<ClientResponse> handleServerError(
        ClientResponse clientResponse,
        HttpStatusCode statusCode) {

        log.error("Server error: {} for request", statusCode);

        // Для серверных ошибок обычно не читаем body для экономии ресурсов
        if (statusCode.value() == 500) {
            return Mono.error(new InternalServerErrorException("Internal server error"));
        } else if (statusCode.value() == 503) {
            return Mono.error(new ServiceUnavailableException("Service unavailable"));
        } else if (statusCode.value() == 504) {
            return Mono.error(new GatewayTimeoutException("Gateway timeout"));
        } else {
            // Общая серверная ошибка
            return Mono.error(new InternalServerErrorException("Server error: " + statusCode));
        }
    }

    /**
     * Фильтр для ограничения скорости запросов (rate limiting).
     *
     * <p>Применяет exponential backoff при превышении лимитов.</p>
     *
     * @return фильтр для rate limiting
     */
    private ExchangeFilterFunction rateLimitingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().value() == 429) { // Too Many Requests
                log.warn("Rate limit exceeded, applying backoff");
                return Mono.error(new WebClientResponseException(
                    "Rate limit exceeded",
                    429,
                    "Too Many Requests",
                    null,
                    null,
                    null
                ));
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Проверяет, является ли заголовок чувствительным для логирования.
     *
     * @param headerName имя заголовка
     * @return true если заголовок содержит чувствительную информацию
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization") ||
            lowerCaseName.contains("token") ||
            lowerCaseName.contains("key") ||
            lowerCaseName.contains("password");
    }

    /**
     * Парсит строковое представление размера в байты.
     *
     * @param size размер в формате "2MB", "1KB" и т.д.
     * @return размер в байтах
     */
    private int parseSize(String size) {
        if (size.endsWith("MB")) {
            return Integer.parseInt(size.substring(0, size.length() - 2)) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Integer.parseInt(size.substring(0, size.length() - 2)) * 1024;
        } else {
            return Integer.parseInt(size);
        }
    }
}