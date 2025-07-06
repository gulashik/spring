package org.gualsh.demo.httpe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Конфигурация WebClient для работы с @HttpExchange.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот класс демонстрирует профессиональную настройку WebClient:
 * </p>
 * <ul>
 * <li>Настройка пула соединений для оптимизации производительности</li>
 * <li>Конфигурация таймаутов для надежности</li>
 * <li>Добавление логирования для мониторинга</li>
 * <li>Обработка ошибок для устойчивости</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * @Autowired
 * private WebClient.Builder webClientBuilder;
 *
 * // Создание клиента для конкретного API
 * WebClient client = webClientBuilder
 *     .baseUrl("https://api.example.com")
 *     .build();
 * }</pre>
 *
 * <h4>Почему именно такая конфигурация</h4>
 * <p>
 * Настройки выбраны на основе best practices:
 * </p>
 * <ul>
 * <li>Connection Pool - для переиспользования соединений</li>
 * <li>Таймауты - для предотвращения зависания</li>
 * <li>Логирование - для отладки и мониторинга</li>
 * <li>Обработка ошибок - для graceful degradation</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfig {

    private final ExternalApiProperties properties;

    /**
     * Создает настроенный WebClient.Builder.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * WebClient.Builder позволяет создавать несколько WebClient
     * с общими настройками, но разными базовыми URL.
     * Это оптимизирует использование ресурсов.
     * </p>
     *
     * <pre>{@code
     * // Создание разных клиентов из одного builder
     * WebClient jsonPlaceholderClient = webClientBuilder
     *     .baseUrl("https://jsonplaceholder.typicode.com")
     *     .build();
     *
     * WebClient reqresClient = webClientBuilder
     *     .baseUrl("https://reqres.in")
     *     .build();
     * }</pre>
     *
     * @return настроенный WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .clientConnector(createHttpConnector())
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize((int) properties.getHttpClient().getPoolSize() * 1024))
            .filter(logRequest())
            .filter(logResponse())
            .filter(handleErrors());
    }

    /**
     * Создает HTTP-коннектор с настройками производительности.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * ReactorClientHttpConnector обеспечивает:
     * </p>
     * <ul>
     * <li>Асинхронные неблокирующие операции</li>
     * <li>Эффективное использование ресурсов</li>
     * <li>Возможность обработки тысяч concurrent connections</li>
     * </ul>
     *
     * <h4>Настройки Connection Pool</h4>
     * <p>
     * Правильная настройка пула соединений критически важна:
     * </p>
     * <ul>
     * <li>maxConnections - максимальное количество соединений</li>
     * <li>maxIdleTime - время жизни idle соединений</li>
     * <li>maxLifeTime - максимальное время жизни соединения</li>
     * </ul>
     *
     * @return настроенный HTTP-коннектор
     */
    private ReactorClientHttpConnector createHttpConnector() {
        ExternalApiProperties.HttpClientConfig httpConfig = properties.getHttpClient();

        // Создание пула соединений
        ConnectionProvider connectionProvider = ConnectionProvider.builder("http-pool")
            .maxConnections(httpConfig.getPoolSize())
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        // Создание HTTP-клиента с настройками
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) httpConfig.getConnectTimeout().toMillis())
            .responseTimeout(httpConfig.getReadTimeout())
            .keepAlive(true);

        return new ReactorClientHttpConnector(httpClient);
    }

    /**
     * Фильтр для логирования исходящих запросов.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * ExchangeFilterFunction позволяет добавлять сквозную функциональность:
     * </p>
     * <ul>
     * <li>Логирование</li>
     * <li>Метрики</li>
     * <li>Авторизация</li>
     * <li>Retry логика</li>
     * </ul>
     *
     * <pre>{@code
     * // Пример кастомного фильтра для авторизации
     * ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
     *     return Mono.just(ClientRequest.from(clientRequest)
     *         .header("Authorization", "Bearer " + getToken())
     *         .build());
     * })
     * }</pre>
     *
     * @return фильтр для логирования запросов
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                log.debug("Header: {} = {}", name, values));
            return Mono.just(clientRequest);
        });
    }

    /**
     * Фильтр для логирования ответов.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Логирование ответов помогает в отладке и мониторинге:
     * </p>
     * <ul>
     * <li>Отслеживание времени ответа</li>
     * <li>Мониторинг статус-кодов</li>
     * <li>Анализ производительности</li>
     * </ul>
     *
     * @return фильтр для логирования ответов
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response: {} {}", clientResponse.statusCode(), clientResponse.headers());
            return Mono.just(clientResponse);
        });
    }

    /**
     * Фильтр для обработки ошибок HTTP.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Централизованная обработка ошибок обеспечивает:
     * </p>
     * <ul>
     * <li>Консистентную обработку ошибок во всем приложении</li>
     * <li>Логирование проблем для анализа</li>
     * <li>Graceful degradation при сбоях</li>
     * </ul>
     *
     * <pre>{@code
     * // Пример обработки конкретных ошибок
     * .filter(ExchangeFilterFunction.ofResponseProcessor(response -> {
     *     if (response.statusCode().is4xxClientError()) {
     *         return response.bodyToMono(String.class)
     *             .flatMap(body -> Mono.error(new ClientException(body)));
     *     }
     *     return Mono.just(response);
     * }))
     * }</pre>
     *
     * @return фильтр для обработки ошибок
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.error("HTTP Error: {} {}",
                    clientResponse.statusCode(),
                    clientResponse.statusCode().getReasonPhrase());

                // Здесь можно добавить специфическую обработку ошибок
                // Например, retry логику или fallback механизмы
            }
            return Mono.just(clientResponse);
        });
    }
}