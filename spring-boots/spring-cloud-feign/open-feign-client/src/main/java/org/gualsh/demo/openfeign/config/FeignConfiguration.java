package org.gualsh.demo.openfeign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.openfeign.exception.FeignErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация OpenFeign клиентов.
 *
 * <p>
 * Этот класс демонстрирует различные способы настройки OpenFeign клиентов.
 * Конфигурация может быть глобальной (применяется ко всем клиентам) или
 * специфичной для конкретного клиента.
 * </p>
 *
 * <h3>Типы конфигурации:</h3>
 * <ul>
 *   <li><strong>@Configuration</strong> - глобальная конфигурация для всех клиентов</li>
 *   <li><strong>configuration в @FeignClient</strong> - конфигурация для конкретного клиента</li>
 *   <li><strong>application.yml</strong> - декларативная конфигурация</li>
 * </ul>
 *
 * <h3>Приоритет конфигурации (от высшего к низшему):</h3>
 * <ol>
 *   <li>Конфигурация в @FeignClient(configuration = ...)</li>
 *   <li>application.yml настройки для конкретного клиента</li>
 *   <li>application.yml настройки по умолчанию</li>
 *   <li>Глобальная @Configuration</li>
 * </ol>
 */
@Slf4j
@Configuration
public class FeignConfiguration {

    /**
     * Настройка уровня логирования для Feign клиентов.
     * <p>
     * Feign поддерживает четыре уровня логирования:
     * </p>
     * <ul>
     *   <li><strong>NONE</strong> - без логирования (по умолчанию)</li>
     *   <li><strong>BASIC</strong> - логирует метод, URL, код ответа и время выполнения</li>
     *   <li><strong>HEADERS</strong> - дополнительно логирует заголовки запроса и ответа</li>
     *   <li><strong>FULL</strong> - логирует заголовки, тело и метаданные для запросов и ответов</li>
     * </ul>
     *
     * <h3>Best Practices:</h3>
     * <ul>
     *   <li>Используйте NONE или BASIC в production для производительности</li>
     *   <li>HEADERS полезен для отладки проблем с авторизацией</li>
     *   <li>FULL используйте только при разработке - может логировать чувствительные данные</li>
     * </ul>
     *
     * @return логгер уровня HEADERS
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    /**
     * Конфигурация повторных попыток (retry) для Feign клиентов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Retryer определяет стратегию повторных попыток при неудачных запросах.
     * По умолчанию Feign не повторяет запросы (Retryer.NEVER_RETRY).
     * </p>
     *
     * <h3>Параметры Retryer.Default:</h3>
     * <ul>
     *   <li><strong>period</strong> - начальный интервал между попытками</li>
     *   <li><strong>maxPeriod</strong> - максимальный интервал между попытками</li>
     *   <li><strong>maxAttempts</strong> - максимальное количество попыток</li>
     * </ul>
     *
     * <h3>Best Practices:</h3>
     * <ul>
     *   <li>Используйте экспоненциальный backoff для избежания DDoS внешних сервисов</li>
     *   <li>Настраивайте retry только для идемпотентных операций (GET, PUT)</li>
     *   <li>Не используйте retry для POST операций без дополнительной логики</li>
     * </ul>
     *
     * @return конфигурацию retryer с экспоненциальным backoff
     */
    @Bean
    public Retryer feignRetryer() {
        // Начальный интервал: 100ms, максимальный: 1000ms, количество попыток: 3
        return new Retryer.Default(100L, TimeUnit.SECONDS.toMillis(1), 3);
    }

    /**
     * Глобальные настройки таймаутов для всех Feign клиентов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Request.Options определяет таймауты соединения и чтения данных.
     * Правильная настройка таймаутов критически важна для производительности
     * и стабильности приложения.
     * </p>
     *
     * <h3>Типы таймаутов:</h3>
     * <ul>
     *   <li><strong>connectTimeout</strong> - время на установку TCP соединения</li>
     *   <li><strong>readTimeout</strong> - время на чтение данных из соединения</li>
     *   <li><strong>followRedirects</strong> - следовать ли HTTP редиректам</li>
     * </ul>
     *
     * <h3>Рекомендации по настройке:</h3>
     * <ul>
     *   <li>connectTimeout: 2-5 секунд для большинства случаев</li>
     *   <li>readTimeout: зависит от типа операции (5-30 секунд)</li>
     *   <li>Учитывайте SLA внешних сервисов при настройке</li>
     * </ul>
     *
     * @return конфигурацию таймаутов
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
            // Connect timeout: 5 секунд
            5, TimeUnit.SECONDS,
            // Read timeout: 10 секунд
            10, TimeUnit.SECONDS,
            // Следовать редиректам
            true
        );
    }

    /**
     * Кастомный декодер ошибок для обработки HTTP ошибок.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * ErrorDecoder позволяет преобразовывать HTTP ошибки (4xx, 5xx) в Java исключения.
     * По умолчанию Feign использует ErrorDecoder.Default, который создает FeignException
     * для всех не-2xx ответов.
     * </p>
     *
     * <h3>Преимущества кастомного ErrorDecoder:</h3>
     * <ul>
     *   <li>Типизированные исключения для разных типов ошибок</li>
     *   <li>Извлечение дополнительной информации из тела ответа</li>
     *   <li>Интеграция с системой обработки ошибок приложения</li>
     * </ul>
     *
     * @param objectMapper Jackson ObjectMapper для парсинга JSON ошибок
     * @return кастомный декодер ошибок
     */
    @Bean
    public ErrorDecoder feignErrorDecoder(ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }

    /**
     * Interceptor для добавления общих заголовков ко всем запросам.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * RequestInterceptor позволяет модифицировать каждый исходящий запрос
     * перед его отправкой. Это удобно для добавления авторизационных заголовков,
     * трейсинга, или других общих заголовков.
     * </p>
     *
     * <h3>Типичные случаи использования:</h3>
     * <ul>
     *   <li>Добавление Authorization заголовков</li>
     *   <li>Добавление correlation ID для трейсинга</li>
     *   <li>Установка User-Agent</li>
     *   <li>Добавление custom заголовков для внешних API</li>
     * </ul>
     *
     * <h3>Best Practices:</h3>
     * <ul>
     *   <li>Используйте отдельные interceptor'ы для разных клиентов при необходимости</li>
     *   <li>Не логируйте чувствительные данные (токены, пароли)</li>
     *   <li>Учитывайте производительность при добавлении тяжелых операций</li>
     * </ul>
     *
     * @return interceptor для добавления общих заголовков
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Добавляем общие заголовки для всех запросов
            requestTemplate.header("User-Agent", "Spring-OpenFeign-Demo/1.0");
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("X-Request-Source", "spring-openfeign-demo");

            // Логируем основную информацию о запросе (без чувствительных данных)
            log.debug("Feign request: {} {}",
                requestTemplate.method(),
                requestTemplate.url());
        };
    }
}