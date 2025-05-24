package org.gualsh.demo.restclient.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Конфигурация для создания и настройки RestClient бинов.
 *
 * Этот класс содержит конфигурацию для различных RestClient инстансов,
 * каждый из которых оптимизирован для работы с конкретными внешними API.
 *
 * Основные возможности конфигурации:
 * - Настройка пула соединений для оптимальной производительности
 * - Настройка таймаутов для различных типов операций
 * - Настройка базовых URL для внешних сервисов
 * - Настройка общих заголовков и обработчиков ошибок
 *
 * @author Demo Author
 * @version 1.0.0
 */
@Configuration
@Slf4j
public class RestClientConfiguration {

    @Value("${external-api.jsonplaceholder.base-url}")
    private String jsonPlaceholderBaseUrl;

    @Value("${external-api.httpbin.base-url}")
    private String httpBinBaseUrl;

    @Value("${app.restclient.connection-pool.max-total:200}")
    private Integer maxTotalConnections;

    @Value("${app.restclient.connection-pool.max-per-route:50}")
    private Integer maxConnectionsPerRoute;

    @Value("${app.restclient.default-timeouts.connection:5000}")
    private Integer defaultConnectionTimeout;

    @Value("${app.restclient.default-timeouts.socket:10000}")
    private Integer defaultSocketTimeout;

    /**
     * Создает настроенный HTTP клиент с пулом соединений.
     *
     * Apache HttpClient 5 выбран как реализация по умолчанию из-за:
     * - Высокой производительности и надежности
     * - Продвинутых возможностей управления соединениями
     * - Поддержки HTTP/2
     * - Гибких настроек таймаутов и retry логики
     *
     * @return настроенный HttpClient
     */
    @Bean
    public HttpClient httpClient() {
        log.info("Создание HttpClient с пулом соединений: max-total={}, max-per-route={}",
            maxTotalConnections, maxConnectionsPerRoute);

        // Настройка пула соединений для оптимальной производительности
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        // Исправлено: используем TimeValue вместо Duration
        connectionManager.setValidateAfterInactivity(org.apache.hc.core5.util.TimeValue.ofSeconds(30));

        // Настройка таймаутов по умолчанию
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(defaultConnectionTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(defaultSocketTimeout))
            .build();

        return HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    /**
     * Создает HttpComponentsClientHttpRequestFactory для интеграции с Spring.
     *
     * @param httpClient настроенный HTTP клиент
     * @return фабрика HTTP запросов
     */
    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // Исправлено: используем ofMillis() вместо ofMilliseconds()
        factory.setConnectTimeout(Duration.ofMillis(defaultConnectionTimeout));
        // setReadTimeout удален - этот таймаут настраивается в RequestConfig выше
        return factory;
    }

    /**
     * Создает RestClient для работы с JSONPlaceholder API.
     *
     * JSONPlaceholder - это популярный fake REST API для тестирования и прототипирования.
     * Этот RestClient оптимизирован для работы с JSON данными.
     *
     * @param requestFactory фабрика HTTP запросов
     * @return настроенный RestClient для JSONPlaceholder
     */
    @Bean("jsonPlaceholderRestClient")
    public RestClient jsonPlaceholderRestClient(HttpComponentsClientHttpRequestFactory requestFactory) {
        log.info("Создание RestClient для JSONPlaceholder API: {}", jsonPlaceholderBaseUrl);

        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(jsonPlaceholderBaseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "Spring-RestClient-Demo/1.0.0")
            .build();
    }

    /**
     * Создает RestClient для работы с HTTPBin API.
     *
     * HTTPBin - это сервис для тестирования HTTP запросов, который возвращает
     * информацию о полученных запросах. Идеален для демонстрации различных
     * типов HTTP операций.
     *
     * @param requestFactory фабрика HTTP запросов
     * @return настроенный RestClient для HTTPBin
     */
    @Bean("httpBinRestClient")
    public RestClient httpBinRestClient(HttpComponentsClientHttpRequestFactory requestFactory) {
        log.info("Создание RestClient для HTTPBin API: {}", httpBinBaseUrl);

        return RestClient.builder()
            .requestFactory(requestFactory)
            .baseUrl(httpBinBaseUrl)
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "Spring-RestClient-Demo/1.0.0")
            .build();
    }

    /**
     * Создает универсальный RestClient для общих целей.
     *
     * Этот RestClient может использоваться для запросов к различным API
     * без предварительно настроенного базового URL.
     *
     * @param requestFactory фабрика HTTP запросов
     * @return универсальный RestClient
     */
    @Bean("genericRestClient")
    public RestClient genericRestClient(HttpComponentsClientHttpRequestFactory requestFactory) {
        log.info("Создание универсального RestClient");

        return RestClient.builder()
            .requestFactory(requestFactory)
            .defaultHeader("User-Agent", "Spring-RestClient-Demo/1.0.0")
            .build();
    }
}