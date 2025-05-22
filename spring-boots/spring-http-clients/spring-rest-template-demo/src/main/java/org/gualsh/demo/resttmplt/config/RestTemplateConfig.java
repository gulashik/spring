package org.gualsh.demo.resttmplt.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Конфигурация для настройки RestTemplate с оптимальными параметрами.
 *
 * <p>Класс предоставляет бины RestTemplate с различными конфигурациями:</p>
 * <ul>
 *   <li>Базовый RestTemplate с настроенными таймаутами</li>
 *   <li>RestTemplate с Apache HttpClient для продвинутых функций</li>
 * </ul>
 *
 * <p>Настройки включают:</p>
 * <ul>
 *   <li>Connection timeout - таймаут установки соединения</li>
 *   <li>Read timeout - таймаут чтения данных</li>
 *   <li>Connection pooling - пул соединений для переиспользования</li>
 *   <li>Retry и error handling стратегии</li>
 * </ul>
 *
 */
@Configuration
public class RestTemplateConfig {

    @Value("${app.rest-template.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${app.rest-template.read-timeout:10000}")
    private int readTimeout;

    @Value("${app.rest-template.max-connections:100}")
    private int maxConnections;

    @Value("${app.rest-template.max-connections-per-route:20}")
    private int maxConnectionsPerRoute;

    /**
     * Создает базовый RestTemplate с настроенными таймаутами.
     *
     * <p>Этот бин используется для простых HTTP запросов где не требуется
     * сложная настройка пула соединений.</p>
     *
     * @param builder RestTemplateBuilder для настройки
     * @return настроенный RestTemplate
     */
    @Bean("basicRestTemplate")
    public RestTemplate basicRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(connectTimeout))
            .setReadTimeout(Duration.ofMillis(readTimeout))
            .build();
    }

    /**
     * Создает продвинутый RestTemplate с Apache HttpClient.
     *
     * <p>Этот бин предоставляет расширенные возможности:</p>
     * <ul>
     *   <li>Пул соединений для переиспользования TCP соединений</li>
     *   <li>Настройка максимального количества соединений</li>
     *   <li>Тонкая настройка таймаутов на уровне HTTP клиента</li>
     *   <li>Поддержка HTTP/2 и других продвинутых протоколов</li>
     * </ul>
     *
     * @return RestTemplate с Apache HttpClient
     */
    @Bean("advancedRestTemplate")
    public RestTemplate advancedRestTemplate() {
        // Настройка пула соединений
        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        // Настройка таймаутов
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        // Создание HTTP клиента
        var httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        // Создание factory для RestTemplate
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }

    /**
     * Создает RestTemplate для тестирования с короткими таймаутами.
     *
     * <p>Используется в интеграционных тестах для быстрого
     * завершения тестов при сбоях соединения.</p>
     *
     * @param builder RestTemplateBuilder для настройки
     * @return RestTemplate для тестов
     */
    @Bean("testRestTemplate")
    public RestTemplate testRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofMillis(1000))
            .setReadTimeout(Duration.ofMillis(2000))
            .build();
    }
}
