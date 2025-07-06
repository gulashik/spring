package org.gualsh.demo.httpe.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.httpe.client.AdvancedHttpExchangeClient;
import org.gualsh.demo.httpe.client.JsonPlaceholderClient;
import org.gualsh.demo.httpe.client.ReqResClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Конфигурация HTTP Exchange клиентов.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот класс демонстрирует как правильно создавать и настраивать
 * HTTP Exchange клиенты в Spring Boot приложении:
 * </p>
 * <ul>
 * <li>Создание HttpServiceProxyFactory для каждого API</li>
 * <li>Настройка WebClient с базовыми URL и конфигурациями</li>
 * <li>Связывание интерфейсов с реализациями</li>
 * <li>Переиспользование общих настроек WebClient</li>
 * </ul>
 *
 * <h4>Пример создания клиента</h4>
 * <pre>{@code
 * // Создание WebClient для конкретного API
 * WebClient webClient = webClientBuilder
 *     .baseUrl("https://api.example.com")
 *     .defaultHeader("User-Agent", "MyApp/1.0")
 *     .build();
 *
 * // Создание HttpServiceProxyFactory
 * HttpServiceProxyFactory factory = HttpServiceProxyFactory
 *     .builderFor(WebClientAdapter.create(webClient))
 *     .build();
 *
 * // Создание клиента
 * MyApiClient client = factory.createClient(MyApiClient.class);
 * }</pre>
 *
 * <h4>Архитектурные принципы</h4>
 * <p>
 * Данная конфигурация следует принципам:
 * </p>
 * <ul>
 * <li>Separation of concerns - каждый API имеет свою конфигурацию</li>
 * <li>DRY - переиспользование общего WebClient.Builder</li>
 * <li>Configuration as code - все настройки в одном месте</li>
 * <li>Type safety - использование типизированных интерфейсов</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class HttpExchangeClientConfig {

    private final WebClient.Builder webClientBuilder;
    private final ExternalApiProperties properties;

    /**
     * Создает клиент для JSONPlaceholder API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * HttpServiceProxyFactory - это фабрика для создания прокси-реализаций
     * интерфейсов с @HttpExchange аннотациями. Процесс создания:
     * </p>
     * <ol>
     * <li>Создается WebClient с базовой конфигурацией</li>
     * <li>WebClient оборачивается в WebClientAdapter</li>
     * <li>HttpServiceProxyFactory создает прокси для интерфейса</li>
     * <li>Все HTTP вызовы перенаправляются через WebClient</li>
     * </ol>
     *
     * <h4>Настройки производительности</h4>
     * <p>
     * Каждый клиент получает индивидуальные настройки:
     * </p>
     * <ul>
     * <li>Базовый URL для оптимизации запросов</li>
     * <li>Специфические таймауты</li>
     * <li>Размер буфера для больших ответов</li>
     * </ul>
     *
     * <pre>{@code
     * // Использование созданного клиента
     * @Autowired
     * private JsonPlaceholderClient client;
     *
     * public Mono<User> getUser(Long id) {
     *     return client.getUser(id)
     *         .doOnSubscribe(s -> log.info("Fetching user {}", id))
     *         .doOnSuccess(user -> log.info("Found user: {}", user.getName()));
     * }
     * }</pre>
     *
     * @return настроенный клиент для JSONPlaceholder API
     */
    @Bean
    public JsonPlaceholderClient jsonPlaceholderClient() {
        log.info("Creating JsonPlaceholderClient with base URL: {}",
            properties.getJsonplaceholder().getBaseUrl());

        WebClient webClient = webClientBuilder
            .baseUrl(properties.getJsonplaceholder().getBaseUrl())
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize((int) properties.getJsonplaceholder().getMaxInMemorySize().toBytes()))
            .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build();

        return factory.createClient(JsonPlaceholderClient.class);
    }

    /**
     * Создает клиент для ReqRes API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Второй клиент демонстрирует как создавать множественные
     * HTTP Exchange клиенты с разными конфигурациями.
     * Каждый клиент независим и может иметь свои настройки.
     * </p>
     *
     * <h4>Изоляция конфигурации</h4>
     * <p>
     * Важные аспекты изоляции:
     * </p>
     * <ul>
     * <li>Отдельный WebClient для каждого API</li>
     * <li>Независимые настройки таймаутов</li>
     * <li>Разные размеры буферов</li>
     * <li>Возможность разного логирования</li>
     * </ul>
     *
     * <pre>{@code
     * // Сравнение с первым клиентом
     * JsonPlaceholderClient client1; // Таймаут 30с, буфер 1MB
     * ReqResClient client2;          // Таймаут 20с, буфер 512KB
     *
     * // Каждый оптимизирован под свой API
     * }</pre>
     *
     * @return настроенный клиент для ReqRes API
     */
    @Bean
    public ReqResClient reqResClient() {
        log.info("Creating ReqResClient with base URL: {}",
            properties.getReqres().getBaseUrl());

        WebClient webClient = webClientBuilder
            .baseUrl(properties.getReqres().getBaseUrl())
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize((int) properties.getReqres().getMaxInMemorySize().toBytes()))
            .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build();

        return factory.createClient(ReqResClient.class);
    }

    /**
     * Создает расширенный клиент для демонстрации дополнительных возможностей.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Этот клиент показывает продвинутые настройки HttpServiceProxyFactory:
     * </p>
     * <ul>
     * <li>Кастомизация factory через builder</li>
     * <li>Добавление дополнительных interceptors</li>
     * <li>Настройка error handling</li>
     * </ul>
     *
     * <h4>Продвинутые настройки</h4>
     * <p>
     * HttpServiceProxyFactory.Builder позволяет:
     * </p>
     * <ul>
     * <li>Настраивать сериализацию/десериализацию</li>
     * <li>Добавлять interceptors для всех методов</li>
     * <li>Конфигурировать error mapping</li>
     * <li>Настраивать threading model</li>
     * </ul>
     *
     * <pre>{@code
     * // Пример расширенной настройки
     * HttpServiceProxyFactory factory = HttpServiceProxyFactory
     *     .builderFor(WebClientAdapter.create(webClient))
     *     .customArgumentResolver(new CustomArgumentResolver())
     *     .build();
     * }</pre>
     *
     * @return расширенный HTTP Exchange клиент
     */
    @Bean
    public AdvancedHttpExchangeClient advancedHttpExchangeClient() {
        log.info("Creating AdvancedHttpExchangeClient for demonstration purposes");

        // Используем JSONPlaceholder URL для демонстрации
        WebClient webClient = webClientBuilder
            .baseUrl(properties.getJsonplaceholder().getBaseUrl())
            .defaultHeader("User-Agent", "HttpExchange-Demo/1.0.0")
            .defaultHeader("Accept", "application/json")
            .build();

        // Расширенная настройка factory
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build();

        return factory.createClient(AdvancedHttpExchangeClient.class);
    }

    /**
     * Создает WebClient для общего использования (опционально).
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Иногда нужен обычный WebClient рядом с HTTP Exchange клиентами.
     * Этот bean показывает как их совмещать.
     * </p>
     *
     * <h4>Когда использовать обычный WebClient</h4>
     * <ul>
     * <li>Сложная логика построения запросов</li>
     * <li>Динамические URL</li>
     * <li>Специфическая обработка ответов</li>
     * <li>Интеграция с legacy кодом</li>
     * </ul>
     *
     * <pre>{@code
     * @Autowired
     * private WebClient generalWebClient;
     *
     * // Использование для сложных запросов
     * public Mono<String> complexRequest() {
     *     return generalWebClient
     *         .get()
     *         .uri(uriBuilder -> uriBuilder
     *             .path("/dynamic")
     *             .queryParam("param", calculateValue())
     *             .build())
     *         .retrieve()
     *         .bodyToMono(String.class);
     * }
     * }</pre>
     *
     * @return общий WebClient
     */
    @Bean
    public WebClient generalWebClient() {
        log.info("Creating general purpose WebClient");

        return webClientBuilder
            .defaultHeader("User-Agent", "HttpExchange-Demo-General/1.0.0")
            .build();
    }
}
