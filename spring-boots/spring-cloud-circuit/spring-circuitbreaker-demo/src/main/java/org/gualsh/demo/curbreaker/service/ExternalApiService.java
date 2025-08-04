package org.gualsh.demo.curbreaker.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.exception.ExternalServiceException;
import org.gualsh.demo.curbreaker.model.ApiResponse;
import org.gualsh.demo.curbreaker.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для работы с внешним API через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данный сервис демонстрирует различные способы использования Circuit Breaker
 * с reactive streams (WebFlux). Показаны как синхронные, так и асинхронные подходы
 * с правильной обработкой ошибок и fallback механизмами.
 * </p>
 *
 * <p><strong>Ключевые принципы:</strong></p>
 * <ul>
 *   <li>Правильная интеграция Circuit Breaker с reactive streams</li>
 *   <li>Meaningful fallback responses</li>
 *   <li>Логирование для мониторинга и диагностики</li>
 *   <li>Обработка различных типов ошибок</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * @Autowired
 * private ExternalApiService apiService;
 *
 * // Асинхронный вызов
 * Mono<ApiResponse> response = apiService.getPostAsync(1L);
 *
 * // Синхронный вызов
 * ApiResponse response = apiService.getPost(1L);
 * }</pre>
 *
 * @author Educational Demo
 * @see CircuitBreaker
 * @see WebClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final WebClient webClient;

    @Qualifier("externalApiCircuitBreaker")
    private final CircuitBreaker circuitBreaker;

    /**
     * Получение поста по ID с использованием Circuit Breaker (асинхронно).
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Данный метод показывает правильную интеграцию Circuit Breaker с reactive streams.
     * CircuitBreakerOperator.of(circuitBreaker) оборачивает Mono и применяет
     * логику Circuit Breaker автоматически.
     * </p>
     *
     * <p><strong>Важные особенности:</strong></p>
     * <ul>
     *   <li>onErrorResume для graceful fallback</li>
     *   <li>doOnError для логирования</li>
     *   <li>timeout для предотвращения долгих ожиданий</li>
     *   <li>Правильная обработка HTTP ошибок</li>
     * </ul>
     *
     * @param id идентификатор поста
     * @return Mono с данными поста или fallback
     */
    public Mono<ApiResponse> getPostAsync(Long id) {
        log.debug("Запрос поста с ID: {}", id);

        return webClient.get()
            .uri("/posts/{id}", id)
            .retrieve()
            .bodyToMono(ApiResponse.class)
            // Применяем Circuit Breaker к reactive stream
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            // Устанавливаем timeout (должен быть меньше Circuit Breaker timeout)
            .timeout(Duration.ofSeconds(4))
            // Логируем ошибки перед fallback
            .doOnError(throwable -> {
                log.error("Ошибка сохранения пользователя через Circuit Breaker: {}",
                    throwable.getMessage());

                // Для операций записи fallback может быть проблематичным
                // В реальном приложении стоит рассмотреть retry pattern или queue
                throw new RuntimeException("Невозможно сохранить пользователя: сервис БД недоступен",
                    throwable);
            });
    }

    /**
     * Удаление пользователя с Circuit Breaker защитой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Операции удаления также требуют осторожности с fallback логикой.
     * Неудачное удаление не должно создавать ложное впечатление успеха.
     * </p>
     *
     * @param id идентификатор пользователя для удаления
     * @return true если удален успешно
     */
    public boolean deleteById(Long id) {
        log.debug("Удаление пользователя с ID: {}", id);

        return circuitBreaker.executeSupplier(() -> {
            simulateDatabaseOperation("deleteById");

            User removed = database.remove(id);
            boolean success = removed != null;

            if (success) {
                log.debug("Пользователь {} удален успешно", id);
            } else {
                log.debug("Пользователь {} не найден для удаления", id);
            }

            return success;
        }).recover(throwable -> {
            log.error("Ошибка удаления пользователя {} через Circuit Breaker: {}",
                id, throwable.getMessage());

            // Для операций удаления не возвращаем false, так как это может ввести в заблуждение
            throw new RuntimeException("Невозможно удалить пользователя: сервис БД недоступен",
                throwable);
        });
    }

    /**
     * Проверка доступности базы данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Health check операции полезны для мониторинга и могут использоваться
     * системами оркестрации для принятия решений о маршрутизации трафика.
     * </p>
     *
     * @return true если БД доступна
     */
    public boolean isHealthy() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                simulateDatabaseOperation("healthCheck");
                return true;
            }).recover(throwable -> false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Имитация операций с базой данных с возможными ошибками.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод симулирует реальные проблемы БД: медленные запросы, timeouts,
     * connection issues. В реальном приложении такие проблемы происходят
     * непредсказуемо.
     * </p>
     *
     * @param operation название операции
     */
    private void simulateDatabaseOperation(String operation) {
        try {
            // Симуляция времени выполнения запроса (50-200ms нормально)
            int delay = ThreadLocalRandom.current().nextInt(50, 200);
            Thread.sleep(delay);

            // Симуляция случайных ошибок (5% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                // Симуляция различных типов ошибок БД
                double errorType = ThreadLocalRandom.current().nextDouble();

                if (errorType < 0.3) {
                    throw new RuntimeException("Database connection timeout");
                } else if (errorType < 0.6) {
                    throw new RuntimeException("Database lock timeout");
                } else {
                    throw new RuntimeException("Database connection pool exhausted");
                }
            }

            // Симуляция медленных запросов (2% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.02) {
                log.warn("Медленный запрос БД для операции: {}", operation);
                Thread.sleep(4000); // больше timeout Circuit Breaker
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Database operation interrupted", e);
        }
    }

    /**
     * Создание fallback пользователя.
     *
     * @param id идентификатор пользователя
     * @return fallback пользователь
     */
    private User createFallbackUser(Long id) {
        return User.builder()
            .id(id)
            .name("Пользователь недоступен")
            .email("unavailable@system.local")
            .additionalInfo("Данные временно недоступны из-за проблем с БД")
            .build();
    }

    /**
     * Создание fallback списка пользователей.
     *
     * @return минимальный список пользователей
     */
    private List<User> createFallbackUserList() {
        return Arrays.asList(
            User.builder()
                .id(1L)
                .name("Системный пользователь")
                .email("system@fallback.local")
                .additionalInfo("Cached данные")
                .build()
        );
    }

    /**
     * Генерация следующего ID для новых пользователей.
     *
     * @return новый уникальный ID
     */
    private Long generateNextId() {
        return database.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L) + 1;
    }

    /**
     * Инициализация тестовых данных.
     */
    private void initializeTestData() {
        database.put(1L, User.builder()
            .id(1L)
            .name("Иван Петров")
            .email("ivan.petrov@example.com")
            .additionalInfo("Администратор системы")
            .build());

        database.put(2L, User.builder()
            .id(2L)
            .name("Мария Сидорова")
            .email("maria.sidorova@example.com")
            .additionalInfo("Менеджер проекта")
            .build());

        database.put(3L, User.builder()
            .id(3L)
            .name("Алексей Иванов")
            .email("alexey.ivanov@example.com")
            .additionalInfo("Разработчик")
            .build());
    }
}