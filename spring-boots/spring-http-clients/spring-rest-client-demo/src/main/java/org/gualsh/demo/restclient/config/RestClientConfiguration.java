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
 * Конфигурация для создания и настройки RestClient бинов.<p>
 *
 * Этот класс содержит конфигурацию для различных RestClient инстансов,
 * каждый из которых оптимизирован для работы с конкретными внешними API.<p>
 *<ul>
 * Использование различных реализаций RestClient в одном приложении необходимо для:
 * <li> Изоляции конфигураций для разных внешних систем (таймауты, заголовки, базовые URL)</li>
 * <li> Возможности независимой настройки параметров подключения для каждой внешней системы</li>
 *  <li>Повышения читаемости кода при работе с несколькими внешними API</li>
 *  <li>Разделения ответственности и упрощения тестирования</li>
 *  <li>Возможности применения специфичных обработчиков ошибок для конкретных API</li>
 *</ul>
 *<ul>
 * Основные возможности конфигурации:
 * <li> Настройка пула соединений для оптимальной производительности</li>
 * <li> Настройка таймаутов для различных типов операций</li>
 * <li> Настройка базовых URL для внешних сервисов</li>
 * <li> Настройка общих заголовков и обработчиков ошибок</li>
 *</ul>
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
     * Создает настроенный HTTP клиент с пулом соединений.</br>
     *
     * <ul>
     * Apache HttpClient 5 выбран как реализация по умолчанию из-за:
     * <li> Высокой производительности и надежности </li>
     * <li> Продвинутых возможностей управления соединениями</li>
     * <li> Поддержки HTTP/2</li>
     * <li> Гибких настроек таймаутов и retry логики</li>
     *</ul>
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
     * HttpComponentsClientHttpRequestFactory - класс из Spring Framework, который <b>используется в качестве фабрики
     *  HTTP-запросов при работе с REST-клиентами</b>, такими как <code>RestClient</code> и <code>RestTemplate</code>.
     *  <p><b>Затем эта фабрика используется при создании RestClient бинов</b>
     *  <li><b>Интеграция Apache HttpClient с Spring</b>: Класс реализует интерфейс и служит адаптером между Apache HttpComponents HttpClient
     *      и HTTP-клиентами Spring ClientHttpRequestFactory</li>
     *  <li><b>Создание HTTP-запросов</b>: Используется для создания объектов ClientHttpRequest,
     *      которые выполняют HTTP-запросы к внешним сервисам.</li>
     *  <li><b>Настройка параметров HTTP-клиента</b>: Позволяет задать различные параметры для выполнения HTTP-запросов</li>
     * @param httpClient настроенный HTTP клиент
     * @return фабрика HTTP запросов
     */
    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectTimeout(Duration.ofMillis(defaultConnectionTimeout));
        factory.setConnectionRequestTimeout(Duration.ofMillis(defaultConnectionTimeout));

        return factory;
    }

    /**
     * Создает RestClient для работы с сервисом JSONPlaceholder API.</br>
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