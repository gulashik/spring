package org.gualsh.demo.curbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Конфигурация Circuit Breaker с дополнительными настройками и мониторингом.
 *
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
 * @Qualifier("externalApiCircuitBreaker")
 * private CircuitBreaker circuitBreaker;
 *
 * // Использование в сервисе
 * circuitBreaker.executeSupplier(() -> externalApiCall());
 * }</pre>
 *
 */
@Slf4j
@Configuration
public class CircuitBreakerConfig {

    /**
     * Получение конкретного Circuit Breaker по имени для внешнего API.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Создание отдельных beans для каждого Circuit Breaker позволяет лучше управлять
     * зависимостями и упрощает тестирование. CircuitBreakerRegistry автоматически
     * создается Spring Boot автоконфигурацией на основе application.yml.
     * </p>
     *
     * <p><strong>Альтернативные подходы:</strong></p>
     * <ul>
     *   <li>Прямое получение из registry в сервисах</li>
     *   <li>Использование @CircuitBreaker аннотации</li>
     *   <li>Программное создание без автоконфигурации</li>
     * </ul>
     *
     * @param registry автоматически созданный Spring Boot registry
     * @return Circuit Breaker для внешнего API
     */
    @Bean(name = "externalApiCircuitBreaker")
    public CircuitBreaker externalApiCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("externalApi");
    }

    /**
     * Circuit Breaker для работы с базой данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Разные сервисы требуют разных настроек Circuit Breaker. База данных обычно
     * более стабильна, чем внешние API, поэтому может иметь более мягкие настройки.
     * </p>
     *
     * @param registry CircuitBreakerRegistry из автоконфигурации
     * @return Circuit Breaker для базы данных
     */
    @Bean(name = "databaseCircuitBreaker")
    public CircuitBreaker databaseCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("database");
    }

    /**
     * Circuit Breaker для email сервиса.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Email сервисы часто имеют rate limiting и могут быть медленными.
     * Для них подходят более мягкие настройки Circuit Breaker.
     * </p>
     *
     * @param registry CircuitBreakerRegistry из автоконфигурации
     * @return Circuit Breaker для email сервиса
     */
    @Bean(name = "emailServiceCircuitBreaker")
    public CircuitBreaker emailServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("emailService");
    }

    /**
     * Настройка event listeners для всех Circuit Breaker instances после инициализации контекста.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * @EventListener(ContextRefreshedEvent.class) вызывается после полной инициализации
     * контекста Spring. Мы получаем CircuitBreakerRegistry через ApplicationContext,
     * чтобы избежать проблем с типами параметров.
     * </p>
     *
     * <p><strong>Типы событий Circuit Breaker:</strong></p>
     * <ul>
     *   <li>SUCCESS - успешный вызов</li>
     *   <li>ERROR - ошибка вызова</li>
     *   <li>STATE_TRANSITION - изменение состояния (CLOSED -> OPEN -> HALF_OPEN)</li>
     *   <li>RESET - сброс Circuit Breaker в исходное состояние</li>
     *   <li>IGNORED_ERROR - игнорируемая ошибка</li>
     *   <li>NOT_PERMITTED - отклоненный вызов (Circuit Breaker в состоянии OPEN)</li>
     * </ul>
     *
     * <p><strong>Интеграция с внешними системами:</strong></p>
     * <p>
     * В этих listeners можно добавить отправку метрик в:
     * </p>
     * <ul>
     *   <li>Prometheus/Grafana для визуализации</li>
     *   <li>DataDog/New Relic для APM мониторинга</li>
     *   <li>Slack/Teams для алертов</li>
     *   <li>Custom logging systems</li>
     * </ul>
     *
     * @param event событие завершения инициализации контекста
     */
    @EventListener(ContextRefreshedEvent.class)
    public void configureEventListeners(ContextRefreshedEvent event) {
        log.info("Настройка event listeners для Circuit Breakers...");

        // Получаем CircuitBreakerRegistry из контекста
        CircuitBreakerRegistry registry = event.getApplicationContext()
            .getBean(CircuitBreakerRegistry.class);

        // Добавляем event listeners для всех Circuit Breaker instances
        registry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            String cbName = circuitBreaker.getName();
            log.debug("Настройка event listeners для Circuit Breaker: {}", cbName);

            // Слушатель изменений состояния (самый важный для мониторинга)
            circuitBreaker.getEventPublisher()
                .onStateTransition(stateEvent -> {
                    log.warn("🔄 Circuit Breaker [{}] изменил состояние: {} -> {} (причина: {})",
                        stateEvent.getCircuitBreakerName(),
                        stateEvent.getStateTransition().getFromState(),
                        stateEvent.getStateTransition().getToState(),
                        stateEvent.getStateTransition().getFromState() != stateEvent.getStateTransition().getToState()
                            ? "threshold exceeded" : "recovery detected");

                    // Здесь можно добавить отправку алертов
                    sendStateChangeAlert(stateEvent.getCircuitBreakerName(),
                        stateEvent.getStateTransition().getFromState(),
                        stateEvent.getStateTransition().getToState());
                });

            // Слушатель успешных вызовов (для статистики)
            circuitBreaker.getEventPublisher()
                .onSuccess(successEvent -> {
                    log.debug("✅ Circuit Breaker [{}] успешный вызов за {}ms",
                        successEvent.getCircuitBreakerName(),
                        successEvent.getElapsedDuration().toMillis());

                    // Можно собирать статистику производительности
                    recordSuccessMetric(successEvent.getCircuitBreakerName(),
                        successEvent.getElapsedDuration().toMillis());
                });

            // Слушатель ошибок (для диагностики)
            circuitBreaker.getEventPublisher()
                .onError(errorEvent -> {
                    log.warn("❌ Circuit Breaker [{}] ошибка за {}ms: {}",
                        errorEvent.getCircuitBreakerName(),
                        errorEvent.getElapsedDuration().toMillis(),
                        errorEvent.getThrowable().getMessage() != null
                            ? errorEvent.getThrowable().getMessage()
                            : errorEvent.getThrowable().getClass().getSimpleName());

                    // Можно собирать статистику ошибок
                    recordErrorMetric(errorEvent.getCircuitBreakerName(),
                        errorEvent.getThrowable());
                });

            // Слушатель отклоненных вызовов (когда Circuit Breaker в состоянии OPEN)
            circuitBreaker.getEventPublisher()
                .onCallNotPermitted(notPermittedEvent -> {
                    log.warn("🚫 Circuit Breaker [{}] отклонил вызов - состояние OPEN",
                        notPermittedEvent.getCircuitBreakerName());

                    // Можно увеличивать счетчик отклоненных вызовов
                    recordRejectedCallMetric(notPermittedEvent.getCircuitBreakerName());
                });

            // Слушатель игнорируемых ошибок (для понимания конфигурации)
            circuitBreaker.getEventPublisher()
                .onIgnoredError(ignoredErrorEvent -> {
                    log.debug("⚠️ Circuit Breaker [{}] игнорировал ошибку: {}",
                        ignoredErrorEvent.getCircuitBreakerName(),
                        ignoredErrorEvent.getThrowable().getMessage() != null
                            ? ignoredErrorEvent.getThrowable().getMessage()
                            : ignoredErrorEvent.getThrowable().getClass().getSimpleName());
                });
        });

        log.info("Event listeners настроены для {} Circuit Breakers",
            registry.getAllCircuitBreakers().size());
    }

    /**
     * Отправка алерта при изменении состояния Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * В продакшене здесь был бы код для отправки уведомлений в Slack, Teams,
     * PagerDuty или другие системы алертинга. Критически важно быстро узнавать
     * о проблемах с внешними сервисами.
     * </p>
     *
     * @param circuitBreakerName имя Circuit Breaker
     * @param fromState предыдущее состояние
     * @param toState новое состояние
     */
    private void sendStateChangeAlert(String circuitBreakerName,
                                      CircuitBreaker.State fromState,
                                      CircuitBreaker.State toState) {
        // В реальном приложении:
        // slackNotificationService.sendAlert(...)
        // pagerDutyService.triggerIncident(...)
        // emailNotificationService.sendAlert(...)

        log.info("📧 Алерт: Circuit Breaker {} изменил состояние {} -> {}",
            circuitBreakerName, fromState, toState);
    }

    /**
     * Запись метрики успешного вызова.
     *
     * @param circuitBreakerName имя Circuit Breaker
     * @param durationMs время выполнения в миллисекундах
     */
    private void recordSuccessMetric(String circuitBreakerName, long durationMs) {
        // В реальном приложении:
        // meterRegistry.timer("circuit.breaker.success", "name", circuitBreakerName)
        //              .record(Duration.ofMillis(durationMs));
    }

    /**
     * Запись метрики ошибки.
     *
     * @param circuitBreakerName имя Circuit Breaker
     * @param throwable исключение
     */
    private void recordErrorMetric(String circuitBreakerName, Throwable throwable) {
        // В реальном приложении:
        // meterRegistry.counter("circuit.breaker.error",
        //                      "name", circuitBreakerName,
        //                      "exception", throwable.getClass().getSimpleName())
        //              .increment();
    }

    /**
     * Запись метрики отклоненного вызова.
     *
     * @param circuitBreakerName имя Circuit Breaker
     */
    private void recordRejectedCallMetric(String circuitBreakerName) {
        // В реальном приложении:
        // meterRegistry.counter("circuit.breaker.rejected", "name", circuitBreakerName)
        //              .increment();
    }
}