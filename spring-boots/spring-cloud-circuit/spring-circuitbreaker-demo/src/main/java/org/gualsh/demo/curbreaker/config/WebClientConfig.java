package org.gualsh.demo.curbreaker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Конфигурация WebClient для HTTP вызовов к внешним сервисам.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * WebClient - это reactive HTTP client в Spring WebFlux. В контексте Circuit Breaker
 * правильная настройка timeouts критически важна, так как они должны быть меньше
 * timeout'а Circuit Breaker для корректной работы.
 * </p>
 *
 * <p><strong>Важные принципы настройки timeouts:</strong></p>
 * <ul>
 *   <li>Connection timeout - время установления соединения</li>
 *   <li>Read timeout - время ожидания ответа</li>
 *   <li>Write timeout - время отправки данных</li>
 *   <li>Response timeout - общий timeout операции (должен быть меньше Circuit Breaker timeout)</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * @Autowired
 * private WebClient webClient;
 *
 * Mono<String> response = webClient.get()
 *     .uri("/api/data")
 *     .retrieve()
 *     .bodyToMono(String.class);
 * }</pre>
 *
 * @author Educational Demo
 * @see WebClient
 * @see HttpClient
 */
@Configuration
public class WebClientConfig {

    /**
     * Base URL для внешнего API из конфигурации.
     */
    @Value("${external-services.api.base-url}")
    private String baseUrl;

    /**
     * Timeout для HTTP операций из конфигурации.
     */
    @Value("${external-services.api.timeout}")
    private int timeout;

    /**
     * Создание настроенного WebClient bean.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Данная конфигурация демонстрирует best practices для настройки WebClient:
     * </p>
     * <ul>
     *   <li>Настройка connection pool для переиспользования соединений</li>
     *   <li>Установка разумных timeout'ов</li>
     *   <li>Конфигурация base URL для удобства использования</li>
     *   <li>Настройка buffer size для больших ответов</li>
     * </ul>
     *
     * <p><strong>Подводные камни:</strong></p>
     * <ul>
     *   <li>Слишком маленькие timeout'ы могут привести к ложным срабатываниям Circuit Breaker</li>
     *   <li>Слишком большие timeout'ы могут заблокировать threads и снизить throughput</li>
     *   <li>Неправильная настройка connection pool может привести к исчерпанию соединений</li>
     * </ul>
     *
     * @return настроенный WebClient
     */
    @Bean
    public WebClient webClient() {
        // Настройка HttpClient с timeouts и connection pool
        HttpClient httpClient = HttpClient.create()
            // Connection timeout - максимальное время на установление соединения
            .option(reactor.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
            // Response timeout - максимальное время ожидания полного ответа
            .responseTimeout(Duration.ofMillis(timeout))
            // Настройка TCP keepalive для переиспользования соединений
            .option(reactor.netty.channel.ChannelOption.SO_KEEPALIVE, true)
            // Размер TCP receive buffer
            .option(reactor.netty.channel.ChannelOption.SO_RCVBUF, 1024 * 1024)
            // Размер TCP send buffer
            .option(reactor.netty.channel.ChannelOption.SO_SNDBUF, 1024 * 1024);

        return WebClient.builder()
            // Устанавливаем base URL для всех запросов
            .baseUrl(baseUrl)
            // Подключаем настроенный HttpClient
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            // Настройка размера буфера для чтения ответов (для больших JSON)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            // Можно добавить default headers, например User-Agent
            .defaultHeader("User-Agent", "Spring-Circuit-Breaker-Demo/1.0")
            .build();
    }
}