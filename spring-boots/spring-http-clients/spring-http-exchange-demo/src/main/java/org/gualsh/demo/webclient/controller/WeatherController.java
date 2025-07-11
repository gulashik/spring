package org.gualsh.demo.webclient.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.WeatherDto;
import org.gualsh.demo.webclient.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * REST контроллер для демонстрации Weather API с HttpExchange.
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Получает текущую погоду для города.
     */
    @GetMapping("/current")
    public Mono<ResponseEntity<WeatherDto>> getCurrentWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting weather for city: {}", city);

        return weatherService.getCurrentWeather(city)
            .map(weather -> {
                log.info("REST: Found weather for {}: {}°C",
                    city, weather.getMain() != null ? weather.getMain().getTemp() : "N/A");
                return ResponseEntity.ok()
                    .header("X-Cache-Key", city.toLowerCase())
                    .header("X-Client-Type", "HttpExchange")
                    .body(weather);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает погоду по координатам.
     */
    @GetMapping("/coordinates")
    public Mono<ResponseEntity<WeatherDto>> getWeatherByCoordinates(
        @RequestParam double lat,
        @RequestParam double lon) {
        log.info("REST: Getting weather by coordinates: lat={}, lon={}", lat, lon);

        return weatherService.getCurrentWeatherByCoordinates(lat, lon)
            .map(weather -> ResponseEntity.ok()
                .header("X-Client-Type", "HttpExchange")
                .body(weather))
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает детальную информацию о погоде.
     */
    @GetMapping("/detailed")
    public Mono<ResponseEntity<WeatherDto>> getDetailedWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting detailed weather for city: {}", city);

        return weatherService.getDetailedWeather(city)
            .map(weather -> ResponseEntity.ok()
                .header("X-Client-Type", "HttpExchange")
                .body(weather))
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Проверяет доступность weather сервиса.
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkWeatherServiceHealth() {
        log.info("REST: Checking weather service health");

        return weatherService.isWeatherServiceAvailable()
            .map(available -> {
                Map<String, Object> status = Map.of(
                    "service", "weather-api",
                    "available", available,
                    "client-type", "HttpExchange",
                    "timestamp", System.currentTimeMillis()
                );

                HttpStatus httpStatus = available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                return ResponseEntity.status(httpStatus).body(status);
            });
    }

    /**
     * Получает погоду для нескольких городов.
     *
     * <p>Новый endpoint, демонстрирующий композицию запросов.</p>
     */
    @PostMapping(value = "/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WeatherDto> getWeatherForCities(@RequestBody List<String> cities) {
        log.info("REST: Getting weather for {} cities", cities.size());

        return weatherService.getWeatherForMultipleCities(cities)
            .doOnNext(weather -> log.debug("REST: Streaming weather for: {}", weather.getName()));
    }

    /**
     * Сравнивает погоду между двумя городами.
     *
     * <p>Демонстрирует бизнес-логику с использованием нескольких API вызовов.</p>
     */
    @GetMapping("/compare")
    public Mono<ResponseEntity<Map<String, Object>>> compareWeather(
        @RequestParam @NotNull String city1,
        @RequestParam @NotNull String city2) {
        log.info("REST: Comparing weather between {} and {}", city1, city2);

        return weatherService.compareWeather(city1, city2)
            .map(comparison -> {
                log.info("REST: Weather comparison completed for {} vs {}", city1, city2);
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(comparison);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }
}