package org.gualsh.demo.curbreaker.config;

import io.netty.channel.ChannelOption;
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
 * <p><strong>Правильная иерархия timeouts:</strong></p>
 * <ul>
 *   <li>HTTP Connection timeout (1-2s) < HTTP Response timeout (4-5s) < Circuit Breaker timeout (5-10s)</li>
 *   <li>Это предотвращает ложные срабатывания Circuit Breaker</li>
 *   <li>Позволяет HTTP клиенту самому обработать timeout'ы</li>
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
 * @see ChannelOption
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
     * Данная конфигурация демонстрирует best practices для настройки WebClient
     * с правильными imports для современных версий Spring Boot 3.x и Netty.
     * </p>
     *
     * <p><strong>Ключевые изменения в Spring Boot 3.x:</strong></p>
     * <ul>
     *   <li>ChannelOption теперь из io.netty.channel.ChannelOption</li>
     *   <li>Reactor Netty обновлен до версии совместимой с Spring Boot 3</li>
     *   <li>Улучшена интеграция с Virtual Threads (Project Loom)</li>
     * </ul>
     *
     * <p><strong>Best practices для WebClient:</strong></p>
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
     *   <li>Неправильные imports могут вызвать ошибки компиляции</li>
     * </ul>
     *
     * @return настроенный WebClient
     */
    @Bean
    public WebClient webClient() {
        // Настройка HttpClient с timeouts и connection pool
        HttpClient httpClient = HttpClient.create()
            // Connection timeout - максимальное время на установление соединения
            // Используем правильный import: io.netty.channel.ChannelOption
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
            // Response timeout - максимальное время ожидания полного ответа
            .responseTimeout(Duration.ofMillis(timeout))
            // Настройка TCP keepalive для переиспользования соединений
            .option(ChannelOption.SO_KEEPALIVE, true)
            // Размер TCP receive buffer (1MB для больших JSON ответов)
            .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
            // Размер TCP send buffer (1MB для больших POST запросов)
            .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
            // Отключаем алгоритм Nagle для уменьшения latency
            .option(ChannelOption.TCP_NODELAY, true);

        return WebClient.builder()
            // Устанавливаем base URL для всех запросов
            .baseUrl(baseUrl)
            // Подключаем настроенный HttpClient
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            // Настройка размера буфера для чтения ответов (для больших JSON)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            // Default headers для идентификации клиента
            .defaultHeader("User-Agent", "Spring-Circuit-Breaker-Demo/1.0")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * Альтернативный WebClient для случаев, когда нужны другие настройки.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Иногда в приложении нужны разные WebClient с разными настройками.
     * Например, один для быстрых API, другой для медленных операций.
     * </p>
     *
     * @return WebClient с увеличенными timeout'ами
     */
    @Bean("slowWebClient")
    public WebClient slowWebClient() {
        // Увеличенные timeout'ы для медленных API
        int slowTimeout = timeout * 3; // 15 секунд вместо 5

        HttpClient slowHttpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, slowTimeout)
            .responseTimeout(Duration.ofMillis(slowTimeout))
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true);

        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(slowHttpClient))
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
            .defaultHeader("User-Agent", "Spring-Circuit-Breaker-Demo-Slow/1.0")
            .build();
    }
}