package org.gualsh.demo.webclient.client;


import org.gualsh.demo.webclient.dto.WeatherDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

/**
 * Декларативный HTTP клиент для OpenWeatherMap API.
 *
 * <p>Демонстрирует использование @HttpExchange для работы с внешними API,
 * требующими API ключи и специфичные параметры запроса.</p>
 */
@HttpExchange(url = "/", accept = "application/json")
public interface WeatherClient {

    /**
     * Получает текущую погоду для города.
     *
     * <p>Основной способ получения погодных данных по названию города.
     * Все необходимые параметры (API ключ, единицы измерения) передаются
     * через query параметры.</p>
     *
     * @param cityName название города
     * @param apiKey API ключ для аутентификации
     * @param units единицы измерения (metric, imperial, kelvin)
     * @return Mono с данными о погоде
     */
    @GetExchange("weather")
    Mono<WeatherDto> getCurrentWeather(
        @RequestParam("q") String cityName,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );

    /**
     * Получает погоду по географическим координатам.
     *
     * <p>Полезно для мобильных приложений с геолокацией.</p>
     *
     * @param lat широта
     * @param lon долгота
     * @param apiKey API ключ
     * @param units единицы измерения
     * @return Mono с данными о погоде
     */
    @GetExchange("weather")
    Mono<WeatherDto> getCurrentWeatherByCoordinates(
        @RequestParam("lat") double lat,
        @RequestParam("lon") double lon,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );

    /**
     * Получает расширенную информацию о погоде.
     *
     * <p>Включает дополнительные параметры для получения более
     * детальной информации о погодных условиях.</p>
     *
     * @param cityName название города
     * @param apiKey API ключ
     * @param units единицы измерения
     * @param mode формат ответа (json, xml, html)
     * @return Mono с расширенными данными о погоде
     */
    @GetExchange("weather")
    Mono<WeatherDto> getDetailedWeather(
        @RequestParam("q") String cityName,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units,
        @RequestParam("mode") String mode
    );

    /**
     * Простая проверка доступности API.
     *
     * <p>Использует фиксированный город для проверки работоспособности сервиса.</p>
     *
     * @param apiKey API ключ
     * @return Mono с любыми данными о погоде (для проверки доступности)
     */
    @GetExchange("weather")
    Mono<WeatherDto> checkApiAvailability(
        @RequestParam("q") String testCity,
        @RequestParam("appid") String apiKey
    );
}
