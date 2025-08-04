package org.gualsh.demo.curbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Circuit Breaker с дополнительными настройками и мониторингом.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данный класс демонстрирует программную конфигурацию Circuit Breaker в дополнение
 * к настройкам из application.yml. Здесь мы добавляем event listeners для мониторинга
 * изменений состояния Circuit Breaker.
 * </p>
 *
 * <p><strong>Основные преимущества программной конфигурации:</strong></p>
 * <ul>
 *   <li>Добавление custom event listeners</li>
 *   <li>Динамическая настройка параметров</li>
 *   <li>Интеграция с внешними системами мониторинга</li>
 *   <li>Более гибкое управление жизненным циклом</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * @Autowired
 * private CircuitBreakerRegistry registry;
 *
 * CircuitBreaker cb = registry.circuitBreaker("externalApi");
 * cb.executeSupplier(() -> externalApiCall());
 * }</pre>
 *
 * @author Educational Demo
 * @see CircuitBreakerRegistry
 * @see CircuitBreaker
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfig {

    /**
     * CircuitBreakerRegistry предоставленный Spring Boot автоконфигурацией.
     * Содержит все Circuit Breaker instances из application.yml.
     */
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Настройка event listeners для всех Circuit Breaker instances.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Event listeners позволяют отслеживать изменения состояния Circuit Breaker
     * и реагировать на них. Это критически важно для мониторинга и алертинга.
     * </p>
     *
     * <p><strong>Типы событий:</strong></p>
     * <ul>
     *   <li>SUCCESS - успешный вызов</li>
     *   <li>ERROR - ошибка вызова</li>
     *   <li>STATE_TRANSITION - изменение состояния (CLOSED -> OPEN -> HALF_OPEN)</li>
     *   <li>RESET - сброс Circuit Breaker в исходное состояние</li>
     * </ul>
     *
     * @return настроенный CircuitBreakerRegistry
     */
    @Bean
    public CircuitBreakerRegistry configureEventListeners() {
        // Добавляем event listeners для всех Circuit Breaker instances
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {

            // Слушатель изменений состояния
            circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    log.info("Circuit Breaker [{}] state transition: {} -> {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState());

                    // Здесь можно добавить отправку метрик в внешние системы
                    // например, в Prometheus, DataDog, или New Relic
                });

            // Слушатель успешных вызовов
            circuitBreaker.getEventPublisher()
                .onSuccess(event -> {
                    log.debug("Circuit Breaker [{}] successful call, duration: {}ms",
                        event.getCircuitBreakerName(),
                        event.getElapsedDuration().toMillis());
                });

            // Слушатель ошибок
            circuitBreaker.getEventPublisher()
                .onError(event -> {
                    log.warn("Circuit Breaker [{}] error call, duration: {}ms, error: {}",
                        event.getCircuitBreakerName(),
                        event.getElapsedDuration().toMillis(),
                        event.getThrowable().getMessage());
                });

            // Слушатель отклоненных вызовов (когда Circuit Breaker в состоянии OPEN)
            circuitBreaker.getEventPublisher()
                .onCallNotPermitted(event -> {
                    log.warn("Circuit Breaker [{}] call not permitted - circuit is OPEN",
                        event.getCircuitBreakerName());
                });
        });

        return circuitBreakerRegistry;
    }

    /**
     * Получение конкретного Circuit Breaker по имени для внедрения в сервисы.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Хотя Circuit Breaker можно получить напрямую из registry в сервисах,
     * создание отдельных beans позволяет лучше управлять зависимостями
     * и упрощает тестирование.
     * </p>
     *
     * @return Circuit Breaker для внешнего API
     */
    @Bean(name = "externalApiCircuitBreaker")
    public CircuitBreaker externalApiCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("externalApi");
    }

    /**
     * Circuit Breaker для работы с базой данных.
     *
     * @return Circuit Breaker для базы данных
     */
    @Bean(name = "databaseCircuitBreaker")
    public CircuitBreaker databaseCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("database");
    }

    /**
     * Circuit Breaker для email сервиса.
     *
     * @return Circuit Breaker для email сервиса
     */
    @Bean(name = "emailServiceCircuitBreaker")
    public CircuitBreaker emailServiceCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("emailService");
    }
}
