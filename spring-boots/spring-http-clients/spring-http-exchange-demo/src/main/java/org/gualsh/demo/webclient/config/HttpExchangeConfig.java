package org.gualsh.demo.webclient.config;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.client.JsonPlaceholderClient;
import org.gualsh.demo.webclient.client.WeatherClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Конфигурация для создания HTTP Exchange клиентов.
 *
 * <p>@HttpExchange требует создания прокси-объектов через HttpServiceProxyFactory.
 * Этот класс настраивает фабрику и создает бины клиентов для внедрения зависимостей.</p>
 *
 * <p>Архитектурные решения:</p>
 * <ul>
 *   <li>Каждый внешний API получает свой собственный WebClient с оптимизированными настройками</li>
 *   <li>HttpServiceProxyFactory создает прокси-интерфейсы для декларативных клиентов</li>
 *   <li>Настройки WebClient (таймауты, базовые URL, заголовки) переиспользуются из WebClientConfig</li>
 *   <li>Каждый клиент может иметь свои специфичные настройки</li>
 * </ul>
 *
 * <p>Преимущества этого подхода:</p>
 * <ul>
 *   <li>Декларативный стиль разработки клиентов</li>
 *   <li>Автоматическая сериализация/десериализация</li>
 *   <li>Встроенная поддержка реактивных типов</li>
 *   <li>Легкое тестирование через мокирование интерфейсов</li>
 *   <li>Централизованная конфигурация HTTP клиентов</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
 * @see HttpServiceProxyFactory
 * @see WebClientAdapter
 * @since Spring 6.0
 */
@Slf4j
@Configuration
public class HttpExchangeConfig {

    /**
     * Создает HttpServiceProxyFactory для JSONPlaceholder API.
     *
     * <p>HttpServiceProxyFactory - это фабрика для создания прокси-объектов
     * интерфейсов, аннотированных @HttpExchange. Она использует WebClient
     * для выполнения HTTP запросов под капотом.</p>
     *
     * @param jsonPlaceholderWebClient настроенный WebClient для JSONPlaceholder API
     * @return HttpServiceProxyFactory для создания клиентов
     */
    @Bean
    public HttpServiceProxyFactory jsonPlaceholderHttpServiceProxyFactory(
        @Qualifier("jsonPlaceholderWebClient") WebClient jsonPlaceholderWebClient) {

        log.info("Creating HttpServiceProxyFactory for JSONPlaceholder API");

        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(jsonPlaceholderWebClient))
            .build();
    }

    /**
     * Создает HttpServiceProxyFactory для Weather API.
     *
     * <p>Отдельная фабрика для Weather API позволяет использовать
     * специфичные настройки (API ключи, заголовки, таймауты).</p>
     *
     * @param weatherWebClient настроенный WebClient для Weather API
     * @return HttpServiceProxyFactory для Weather клиентов
     */
    @Bean
    public HttpServiceProxyFactory weatherHttpServiceProxyFactory(
        @Qualifier("weatherWebClient") WebClient weatherWebClient) {

        log.info("Creating HttpServiceProxyFactory for Weather API");

        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(weatherWebClient))
            .build();
    }

    /**
     * Создает бин JsonPlaceholderClient.
     *
     * <p>HttpServiceProxyFactory.createClient() создает прокси-объект,
     * который реализует интерфейс JsonPlaceholderClient. При вызове методов
     * интерфейса, прокси автоматически преобразует их в HTTP запросы.</p>
     *
     * @param factory фабрика для создания прокси
     * @return готовый к использованию JsonPlaceholderClient
     */
    @Bean
    public JsonPlaceholderClient jsonPlaceholderClient(
        @Qualifier("jsonPlaceholderHttpServiceProxyFactory") HttpServiceProxyFactory factory) {

        log.info("Creating JsonPlaceholderClient proxy");

        return factory.createClient(JsonPlaceholderClient.class);
    }

    /**
     * Создает бин WeatherClient.
     *
     * <p>Аналогично JsonPlaceholderClient, но использует отдельную фабрику
     * с настройками, специфичными для Weather API.</p>
     *
     * @param factory фабрика для Weather API
     * @return готовый к использованию WeatherClient
     */
    @Bean
    public WeatherClient weatherClient(
        @Qualifier("weatherHttpServiceProxyFactory") HttpServiceProxyFactory factory) {

        log.info("Creating WeatherClient proxy");

        return factory.createClient(WeatherClient.class);
    }

    /**
     * Создает дополнительную фабрику с кастомными настройками.
     *
     * <p>Демонстрирует расширенную настройку HttpServiceProxyFactory
     * с дополнительными возможностями.</p>
     *
     * @param webClient базовый WebClient
     * @return HttpServiceProxyFactory с расширенными настройками
     */
    @Bean
    public HttpServiceProxyFactory customHttpServiceProxyFactory(
        @Qualifier("webClient") WebClient webClient) {

        log.info("Creating custom HttpServiceProxyFactory with extended settings");

        return HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
        // Можно добавить дополнительные настройки:
        // .customArgumentResolver(...) - кастомные резолверы аргументов
        // .conversionService(...) - кастомный сервис конвертации
        // blockTimeout удален в Spring 6.1 - используйте настройки WebClient
            .build();
    }

    /**
     * Пример создания универсального клиента.
     *
     * <p>Показывает как можно создать клиент на основе базового WebClient
     * без специфичных настроек для конкретного API.</p>
     *
     * @param factory универсальная фабрика
     * @return JsonPlaceholderClient на основе базового WebClient
     */
    @Bean("universalJsonPlaceholderClient")
    public JsonPlaceholderClient universalJsonPlaceholderClient(
        @Qualifier("customHttpServiceProxyFactory") HttpServiceProxyFactory factory) {

        log.info("Creating universal JsonPlaceholderClient");

        // Этот клиент использует базовые настройки WebClient
        // и может быть менее оптимизирован для конкретного API
        return factory.createClient(JsonPlaceholderClient.class);
    }
}