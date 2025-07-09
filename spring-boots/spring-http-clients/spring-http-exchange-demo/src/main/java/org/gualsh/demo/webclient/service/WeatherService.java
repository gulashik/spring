package org.gualsh.demo.webclient.service;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.client.WeatherClient;
import org.gualsh.demo.webclient.dto.WeatherDto;
import org.gualsh.demo.webclient.exception.CustomWebClientExceptions.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обновленный сервис для работы с OpenWeatherMap API через @HttpExchange клиент.
 *
 * <p>Основные изменения по сравнению с версией на WebClient:</p>
 * <ul>
 *   <li>Использует декларативный WeatherClient вместо прямых вызовов WebClient</li>
 *   <li>Упрощена передача параметров - автоматическая обработка query параметров</li>
 *   <li>Сфокусирован на бизнес-логике и обработке погодных данных</li>
 *   <li>Сохранена функциональность кэширования и retry</li>
 * </ul>
 *
 * <p>Преимущества HttpExchange для внешних API:</p>
 * <ul>
 *   <li>Автоматическая передача параметров запроса</li>
 *   <li>Упрощенная обработка API ключей</li>
 *   <li>Декларативная настройка различных endpoint'ов</li>
 *   <li>Консистентная обработка ответов</li>
 * </ul>
 *
 * @author Demo
 * @version 2.0
 * @see WeatherClient
 */
@Slf4j
@Service
public class WeatherService {

    private final WeatherClient client;
    private final String apiKey;
    private final int maxAttempts;
    private final long delay;

    /**
     * Конструктор с внедрением HttpExchange клиента.
     *
     * @param client HttpExchange клиент для Weather API
     * @param apiKey API ключ для OpenWeatherMap
     * @param maxAttempts максимальное количество попыток
     * @param delay задержка между попытками
     */
    public WeatherService(
        WeatherClient client,
        @Value("${external-api.weather.api-key}") String apiKey,
        @Value("${external-api.weather.max-attempts:2}") int maxAttempts,
        @Value("${external-api.weather.delay:2000}") long delay) {
        this.client = client;
        this.apiKey = apiKey;
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        log.info("WeatherService initialized with HttpExchange client, maxAttempts: {}, delay: {}ms",
            maxAttempts, delay);
    }

    /**
     * Получает текущую погоду для города с кэшированием.
     *
     * <p>Сравнение с WebClient версией:</p>
     * <pre>{@code
     * // Старая версия (WebClient):
     * return webClient.get()
     *     .uri(uriBuilder -> uriBuilder
     *         .path("/weather")
     *         .queryParam("q", cityName)
     *         .queryParam("appid", apiKey)
     *         .queryParam("units", "metric")
     *         .build())
     *     .retrieve()
     *     .bodyToMono(WeatherDto.class)
     *
     * // Новая версия (HttpExchange):
     * return client.getCurrentWeather(cityName, apiKey, "metric")
     * }</pre>
     *
     * @param cityName название города
     * @return Mono с данными о погоде
     */
    @Cacheable(value = "weather", key = "#cityName.toLowerCase()")
    @Retryable(
        value = {ServiceUnavailableException.class,
            GatewayTimeoutException.class,
            InternalServerErrorException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    public Mono<WeatherDto> getCurrentWeather(String cityName) {
        log.debug("Fetching current weather for city: {}", cityName);

        return client.getCurrentWeather(cityName, apiKey, "metric")
            .map(this::enrichWeatherData)
            .doOnSuccess(weather -> log.info("Successfully fetched weather for {}: {}°C",
                cityName, weather.getMain() != null ? weather.getMain().getTemp() : "N/A"))
            .doOnError(error -> log.error("Error fetching weather for {}: {}", cityName, error.getMessage()));
    }

    /**
     * Получает погоду по географическим координатам.
     *
     * <p>HttpExchange автоматически обрабатывает множественные query параметры.</p>
     *
     * @param lat широта
     * @param lon долгота
     * @return Mono с данными о погоде
     */
    @Cacheable(value = "weather", key = "'coords_' + #lat + '_' + #lon")
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 2000)
    )
    public Mono<WeatherDto> getCurrentWeatherByCoordinates(double lat, double lon) {
        log.debug("Fetching weather by coordinates: lat={}, lon={}", lat, lon);

        return client.getCurrentWeatherByCoordinates(lat, lon, apiKey, "metric")
            .map(this::enrichWeatherData)
            .doOnSuccess(weather -> log.info("Successfully fetched weather by coordinates: {}°C",
                weather.getMain() != null ? weather.getMain().getTemp() : "N/A"))
            .doOnError(error -> log.error("Error fetching weather by coordinates: {}", error.getMessage()));
    }

    /**
     * Получает расширенную информацию о погоде с прогнозом.
     *
     * <p>Демонстрирует передачу дополнительных параметров через HttpExchange.</p>
     *
     * @param cityName название города
     * @return Mono с расширенными данными о погоде
     */
    public Mono<WeatherDto> getDetailedWeather(String cityName) {
        log.debug("Fetching detailed weather for city: {}", cityName);

        return client.getDetailedWeather(cityName, apiKey, "metric", "json")
            .map(this::enrichWeatherData)
            .doOnSuccess(weather -> log.info("Successfully fetched detailed weather for {}", cityName))
            .doOnError(error -> log.error("Error fetching detailed weather for {}: {}", cityName, error.getMessage()));
    }

    /**
     * Проверяет доступность weather API.
     *
     * <p>Использует фиксированный тестовый город для проверки.</p>
     *
     * @return Mono<Boolean> с результатом проверки
     */
    public Mono<Boolean> isWeatherServiceAvailable() {
        return client.checkApiAvailability("London", apiKey)
            .map(response -> true)
            .onErrorReturn(false)
            .doOnNext(available -> log.debug("Weather service availability: {}", available));
    }

    /**
     * Метод восстановления при недоступности weather API.
     *
     * <p>Возвращает базовые погодные данные при сбоях.</p>
     *
     * @param ex исключение
     * @param cityName название города
     * @return Mono с данными по умолчанию
     */
    @Recover
    public Mono<WeatherDto> recoverCurrentWeather(Exception ex, String cityName) {
        log.warn("Recovering from weather API failure for city {}: {}", cityName, ex.getMessage());

        WeatherDto defaultWeather = WeatherDto.builder()
            .name(cityName)
            .main(WeatherDto.MainWeatherDto.builder()
                .temp(20.0) // 20°C по умолчанию
                .humidity(50)
                .pressure(1013)
                .build())
            .build();

        return Mono.just(defaultWeather);
    }

    /**
     * Получает погоду для множественных городов.
     *
     * <p>Демонстрирует композицию нескольких запросов через HttpExchange клиент.</p>
     *
     * @param cities список названий городов
     * @return Flux с данными о погоде для каждого города
     */
    public Flux<WeatherDto> getWeatherForMultipleCities(List<String> cities) {
        log.debug("Fetching weather for {} cities", cities.size());

        return Flux.fromIterable(cities)
            .flatMap(city -> getCurrentWeather(city)
                .onErrorReturn(createDefaultWeather(city)), 3) // Параллельность = 3
            .doOnComplete(() -> log.info("Completed weather fetch for {} cities", cities.size()));
    }

    /**
     * Сравнивает погоду между двумя городами.
     *
     * <p>Пример бизнес-логики, использующей несколько запросов к API.</p>
     *
     * @param city1 первый город
     * @param city2 второй город
     * @return Mono с результатом сравнения
     */
    public Mono<Map<String, Object>> compareWeather(String city1, String city2) {
        log.debug("Comparing weather between {} and {}", city1, city2);

        Mono<WeatherDto> weather1 = getCurrentWeather(city1);
        Mono<WeatherDto> weather2 = getCurrentWeather(city2);

        return Mono.zip(weather1, weather2)
            .map(tuple -> {
                WeatherDto w1 = tuple.getT1();
                WeatherDto w2 = tuple.getT2();

                double temp1 = w1.getMain() != null ? w1.getMain().getTemp() : 0.0;
                double temp2 = w2.getMain() != null ? w2.getMain().getTemp() : 0.0;

                // Используем HashMap для избежания проблем с типизацией Map.of()
                Map<String, Object> result = new HashMap<>();
                result.put("city1", city1);
                result.put("city2", city2);
                result.put("temperature1", temp1);
                result.put("temperature2", temp2);
                result.put("temperatureDifference", Math.abs(temp1 - temp2));
                result.put("warmerCity", temp1 > temp2 ? city1 : city2);
                result.put("timestamp", LocalDateTime.now());

                return result;
            })
            .doOnSuccess(result -> log.info("Successfully compared weather between {} and {}", city1, city2));
    }

    /**
     * Обогащает данные о погоде дополнительными вычислениями.
     *
     * @param weather исходные данные о погоде
     * @return обогащенные данные
     */
    private WeatherDto enrichWeatherData(WeatherDto weather) {
        if (weather.getMain() != null) {
            Double temp = weather.getMain().getTemp();
            if (temp != null && temp > 100) { // Скорее всего Кельвины
                weather.getMain().setTemp(kelvinToCelsius(temp));
            }
        }
        return weather;
    }

    /**
     * Конвертирует температуру из Кельвинов в Цельсий.
     *
     * @param kelvin температура в Кельвинах
     * @return температура в Цельсиях
     */
    private Double kelvinToCelsius(Double kelvin) {
        return kelvin != null ? kelvin - 273.15 : null;
    }

    /**
     * Создает погодные данные по умолчанию для города.
     *
     * @param cityName название города
     * @return WeatherDto с данными по умолчанию
     */
    private WeatherDto createDefaultWeather(String cityName) {
        return WeatherDto.builder()
            .name(cityName)
            .main(WeatherDto.MainWeatherDto.builder()
                .temp(20.0)
                .humidity(50)
                .pressure(1013)
                .build())
            .build();
    }
}