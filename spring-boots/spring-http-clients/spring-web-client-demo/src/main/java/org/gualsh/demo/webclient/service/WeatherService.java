package org.gualsh.demo.webclient.service;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.WeatherDto;
import org.gualsh.demo.webclient.exception.CustomWebClientExceptions.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Сервис для работы с OpenWeatherMap API.
 *
 * <p>Предоставляет методы для получения данных о текущей погоде с кэшированием
 * и обработкой ошибок. Демонстрирует работу с внешними API, требующими API ключи.</p>
 *
 * <p>Особенности:</p>
 * <ul>
 *   <li>Кэширование погодных данных на 5 минут</li>
 *   <li>Retry механизм для сетевых сбоев</li>
 *   <li>Graceful degradation при недоступности сервиса</li>
 *   <li>Обработка rate limiting (HTTP 429)</li>
 * </ul>
 *
 * @see WeatherDto
 */
@Slf4j
@Service
public class WeatherService {

    private final WebClient weatherWebClient;
    private final String apiKey;
    private final int maxAttempts;
    private final long delay;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param weatherWebClient WebClient для Weather API
     * @param apiKey API ключ для OpenWeatherMap
     * @param maxAttempts максимальное количество попыток
     * @param delay задержка между попытками
     */
    public WeatherService(
        @Qualifier("weatherWebClient") WebClient weatherWebClient,
        @Value("${external-api.weather.api-key}") String apiKey,
        @Value("${external-api.weather.max-attempts:2}") int maxAttempts,
        @Value("${external-api.weather.delay:2000}") long delay) {
        this.weatherWebClient = weatherWebClient;
        this.apiKey = apiKey;
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        log.info("WeatherService initialized with maxAttempts: {}, delay: {}ms", maxAttempts, delay);
    }

    /**
     * Получает текущую погоду для города с кэшированием.
     *
     * <p>Кэширует результат на 5 минут для снижения нагрузки на API
     * и экономии лимитов запросов.</p>
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

        return weatherWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("q", cityName)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric") // Цельсий
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals,
                response -> {
                    log.warn("City not found: {}", cityName);
                    return Mono.error(new ResourceNotFoundException("City not found: " + cityName, cityName));
                })
            .onStatus(HttpStatus.UNAUTHORIZED::equals,
                response -> {
                    log.error("Invalid API key for weather service");
                    return Mono.error(new AuthenticationException("Invalid API key"));
                })
            .onStatus(status -> status.value() == 429,
                response -> {
                    log.warn("Rate limit exceeded for weather API");
                    return Mono.error(new RateLimitExceededException("Rate limit exceeded"));
                })
            .onStatus(HttpStatus.SERVICE_UNAVAILABLE::equals,
                response -> Mono.error(new ServiceUnavailableException("Weather service unavailable")))
            .onStatus(HttpStatus.GATEWAY_TIMEOUT::equals,
                response -> Mono.error(new GatewayTimeoutException("Weather service timeout")))
            .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
                response -> Mono.error(new InternalServerErrorException("Weather service internal error")))
            .bodyToMono(WeatherDto.class)
            .doOnSuccess(weather -> log.info("Successfully fetched weather for {}: {}°C",
                cityName, weather.getMain() != null ? weather.getMain().getTemp() : "N/A"))
            .doOnError(error -> log.error("Error fetching weather for {}: {}", cityName, error.getMessage()));
    }

    /**
     * Получает погоду по географическим координатам.
     *
     * <p>Полезно для мобильных приложений с геолокацией.</p>
     *
     * @param lat широта
     * @param lon долгота
     * @return Mono с данными о погоде
     */
    @Cacheable(value = "weather", key = "'coords_' + #lat + '_' + #lon")
    public Mono<WeatherDto> getCurrentWeatherByCoordinates(double lat, double lon) {
        log.debug("Fetching weather by coordinates: lat={}, lon={}", lat, lon);

        return weatherWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(WeatherDto.class)
            .retryWhen(Retry.backoff(maxAttempts, Duration.ofMillis(delay)))
            .doOnSuccess(weather -> log.info("Successfully fetched weather by coordinates: {}°C",
                kelvinToCelsius(weather.getMain().getTemp())))
            .doOnError(error -> log.error("Error fetching weather by coordinates: {}", error.getMessage()));
    }

    /**
     * Получает расширенную информацию о погоде с прогнозом.
     *
     * <p>Демонстрирует работу с более сложными API endpoints.</p>
     *
     * @param cityName название города
     * @return Mono с расширенными данными о погоде
     */
    public Mono<WeatherDto> getDetailedWeather(String cityName) {
        log.debug("Fetching detailed weather for city: {}", cityName);

        return weatherWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("q", cityName)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("mode", "json")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(WeatherDto.class)
            .map(this::enrichWeatherData)
            .doOnSuccess(weather -> log.info("Successfully fetched detailed weather for {}", cityName))
            .doOnError(error -> log.error("Error fetching detailed weather for {}: {}", cityName, error.getMessage()));
    }

    /**
     * Метод восстановления при недоступности weather API.
     *
     * <p>Возвращает базовые погодные данные или данные из кэша.</p>
     *
     * @param ex исключение
     * @param cityName название города
     * @return Mono с данными по умолчанию
     */
    @Recover
    public Mono<WeatherDto> recoverCurrentWeather(Exception ex, String cityName) {
        log.warn("Recovering from weather API failure for city {}: {}", cityName, ex.getMessage());

        // Возвращаем данные по умолчанию
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
     * Обогащает данные о погоде дополнительными вычислениями.
     *
     * @param weather исходные данные о погоде
     * @return обогащенные данные
     */
    private WeatherDto enrichWeatherData(WeatherDto weather) {
        if (weather.getMain() != null) {
            // Конвертируем температуру из Кельвинов в Цельсий если нужно
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
     * Проверяет доступность weather API.
     *
     * @return Mono<Boolean> с результатом проверки
     */
    public Mono<Boolean> isWeatherServiceAvailable() {
        return weatherWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/weather")
                .queryParam("q", "London")
                .queryParam("appid", apiKey)
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> true)
            .onErrorReturn(false)
            .doOnNext(available -> log.debug("Weather service availability: {}", available));
    }
}