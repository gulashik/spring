package org.gualsh.demo.curbreaker.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.exception.ExternalServiceException;
import org.gualsh.demo.curbreaker.model.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для работы с внешним API через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данный сервис демонстрирует ДВА различных способа использования Circuit Breaker:
 * 1. ПРОГРАММНЫЙ подход - с явным использованием CircuitBreaker API
 * 2. АННОТАЦИОННЫЙ подход - с использованием @CircuitBreaker аннотации
 * </p>
 *
 * <p><strong>Сравнение подходов:</strong></p>
 * <ul>
 *   <li><strong>Программный:</strong> больше контроля, лучше для reactive streams</li>
 *   <li><strong>Аннотационный:</strong> меньше кода, декларативный стиль, AOP</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * @Autowired
 * private ExternalApiService apiService;
 *
 * // Программный подход
 * Mono<ApiResponse> response1 = apiService.getPostAsync(1L);
 * ApiResponse response2 = apiService.getPost(1L);
 *
 * // Аннотационный подход  
 * CompletableFuture<ApiResponse> response3 = apiService.getPostWithAnnotation(1L);
 * Mono<ApiResponse> response4 = apiService.getPostReactiveWithAnnotation(1L);
 * }</pre>
 *
 * @author Educational Demo
 * @see CircuitBreaker
 * @see WebClient
 */
@Slf4j
@Service
public class ExternalApiService {

    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    /**
     * Конструктор с явным указанием @Qualifier для Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Lombok @RequiredArgsConstructor НЕ УМЕЕТ корректно обрабатывать @Qualifier аннотации.
     * Поэтому для dependency injection с qualifiers необходимо использовать явные конструкторы.
     * </p>
     *
     * <p><strong>Альтернативы:</strong></p>
     * <ul>
     *   <li>Явный конструктор (рекомендуется) - показан здесь</li>
     *   <li>Field injection с @Autowired + @Qualifier</li>
     *   <li>Setter injection (не рекомендуется)</li>
     * </ul>
     *
     * @param webClient настроенный WebClient для HTTP вызовов
     * @param circuitBreaker Circuit Breaker для внешнего API
     */
    public ExternalApiService(
        WebClient webClient,
        @Qualifier("externalApiCircuitBreaker") CircuitBreaker circuitBreaker
    ) {
        this.webClient = webClient;
        this.circuitBreaker = circuitBreaker;
    }

    // ===============================
    // ПРОГРАММНЫЙ ПОДХОД (оригинальные методы)
    // ===============================

    /**
     * Получение поста по ID (синхронная версия) - ПРОГРАММНЫЙ подход.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Иногда требуется синхронный API. В этом случае мы блокируем reactive stream
     * и используем CircuitBreaker.executeSupplier для синхронного выполнения.
     * Fallback обрабатывается через try-catch, так как executeSupplier не имеет recover().
     * </p>
     *
     * <p><strong>Важно:</strong></p>
     * <p>
     * block() блокирует текущий поток, что может негативно влиять на производительность
     * в reactive приложениях. Используйте только когда действительно необходимо.
     * </p>
     *
     * @param id идентификатор поста
     * @return данные поста или fallback
     */
    public ApiResponse getPost(Long id) {
        log.debug("Синхронный запрос поста с ID: {} (программный подход)", id);

        try {
            return circuitBreaker.executeSupplier(() -> {
                try {
                    return webClient.get()
                        .uri("/posts/{id}", id)
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
                        .timeout(Duration.ofSeconds(4))
                        .block();
                } catch (WebClientResponseException e) {
                    log.error("HTTP ошибка при получении поста {}: {} - {}",
                        id, e.getStatusCode(), e.getResponseBodyAsString());
                    throw new ExternalServiceException(
                        "HTTP ошибка: " + e.getStatusCode(),
                        e,
                        e.getResponseBodyAsString()
                    );
                } catch (Exception e) {
                    log.error("Техническая ошибка при получении поста {}: {}",
                        id, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    throw new ExternalServiceException("Техническая ошибка", e);
                }
            });
        } catch (Exception e) {
            // Fallback при любых ошибках (включая Circuit Breaker)
            log.warn("Circuit Breaker fallback для поста {}: {}",
                id, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return createFallbackPost(id);
        }
    }

    /**
     * Получение поста по ID с использованием Circuit Breaker (асинхронно) - ПРОГРАММНЫЙ подход.
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
        log.debug("Запрос поста с ID: {} (программный подход)", id);

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
                log.error("Ошибка при получении поста {}: {}", id, throwable.getMessage());
            })
            // Fallback при любой ошибке
            .onErrorResume(throwable -> {
                log.warn("Использование fallback для поста {}: {}", id, throwable.getMessage());
                return Mono.just(createFallbackPost(id));
            })
            // Логируем успешные результаты
            .doOnSuccess(response -> {
                log.debug("Успешно получен пост: {}", response.getId());
            });
    }

    // ===============================
    // АННОТАЦИОННЫЙ ПОДХОД (новые методы)
    // ===============================

    /**
     * Получение поста по ID с использованием @CircuitBreaker аннотации - АННОТАЦИОННЫЙ подход.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод демонстрирует аннотационный подход к Circuit Breaker.
     * Spring AOP автоматически применяет Circuit Breaker логику перед выполнением метода.
     * Fallback метод вызывается автоматически при срабатывании Circuit Breaker.
     * </p>
     *
     * <p><strong>Ключевые особенности:</strong></p>
     * <ul>
     *   <li>Декларативный стиль - логика Circuit Breaker скрыта в аннотации</li>
     *   <li>Автоматический вызов fallback метода</li>
     *   <li>Меньше кода - нет явной обработки ошибок</li>
     *   <li>Работает через AOP прокси</li>
     * </ul>
     *
     * <p><strong>Важно:</strong></p>
     * <p>
     * CompletableFuture используется для совместимости с аннотационным подходом.
     * Для чисто reactive подходов лучше использовать программный вариант.
     * </p>
     *
     * @param id идентификатор поста
     * @return CompletableFuture с данными поста или fallback
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "externalApi", fallbackMethod = "getPostAnnotationFallback")
    @TimeLimiter(name = "externalApi")
    @Retry(name = "externalApi")
    public CompletableFuture<ApiResponse> getPostWithAnnotation(Long id) {
        log.debug("Запрос поста с ID: {} (аннотационный подход)", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                simulateExternalApiCall("getPost-" + id);
                
                ApiResponse response = webClient.get()
                    .uri("/posts/{id}", id)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
                
                log.debug("Успешно получен пост через аннотации: {}", response.getId());
                return response;
            } catch (WebClientResponseException e) {
                log.error("HTTP ошибка при получении поста {}: {} - {}",
                    id, e.getStatusCode(), e.getResponseBodyAsString());
                throw new ExternalServiceException(
                    "HTTP ошибка: " + e.getStatusCode(),
                    e,
                    e.getResponseBodyAsString()
                );
            } catch (Exception e) {
                log.error("Техническая ошибка при получении поста {}: {}",
                    id, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                throw new ExternalServiceException("Техническая ошибка", e);
            }
        });
    }

    /**
     * Fallback метод для getPostWithAnnotation.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Fallback методы должны иметь такую же сигнатуру как оригинальный метод,
     * плюс дополнительный параметр Exception. Spring автоматически передает
     * исключение, которое привело к вызову fallback.
     * </p>
     *
     * <p><strong>Порядок обработки fallback:</strong></p>
     * <ol>
     *   <li>Spring ищет fallback с точным типом исключения</li>
     *   <li>Если не найден, ищет fallback с родительским типом</li>
     *   <li>В итоге использует fallback с Exception</li>
     * </ol>
     *
     * @param id идентификатор поста  
     * @param ex исключение, которое привело к fallback
     * @return CompletableFuture с fallback данными
     */
    public CompletableFuture<ApiResponse> getPostAnnotationFallback(Long id, Exception ex) {
        log.warn("Fallback для поста {} (аннотационный подход): {}",
            id, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        
        return CompletableFuture.completedFuture(
            ApiResponse.builder()
                .id(id)
                .title("Fallback через аннотации")
                .body("Данные получены через fallback механизм аннотационного подхода. " +
                    "Внешний API временно недоступен.")
                .userId(0L)
                .build()
        );
    }

    /**
     * Получение поста через аннотации с reactive подходом - ЭКСПЕРИМЕНТАЛЬНЫЙ.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод показывает попытку использования аннотаций с reactive streams.
     * ВАЖНО: это может работать не во всех случаях, так как аннотационный подход
     * Resilience4j лучше работает с CompletableFuture, чем с Mono/Flux.
     * </p>
     *
     * <p><strong>Рекомендация:</strong></p>
     * <p>
     * Для reactive приложений лучше использовать программный подход
     * с CircuitBreakerOperator.of(circuitBreaker).
     * </p>
     *
     * @param id идентификатор поста
     * @return Mono с данными поста или fallback
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "externalApi", fallbackMethod = "getPostReactiveFallback")
    public Mono<ApiResponse> getPostReactiveWithAnnotation(Long id) {
        log.debug("Reactive запрос поста с ID: {} (аннотационный подход)", id);

        return Mono.fromCallable(() -> {
                simulateExternalApiCall("getPostReactive-" + id);
                return "success";
            })
            .then(webClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofSeconds(4))
            )
            .doOnSuccess(response -> {
                log.debug("Успешно получен пост через reactive аннотации: {}", response.getId());
            });
    }

    /**
     * Fallback для reactive метода с аннотациями.
     *
     * @param id идентификатор поста
     * @param ex исключение
     * @return Mono с fallback данными
     */
    public Mono<ApiResponse> getPostReactiveFallback(Long id, Exception ex) {
        log.warn("Reactive fallback для поста {} (аннотационный подход): {}",
            id, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        
        return Mono.just(
            ApiResponse.builder()
                .id(id)
                .title("Reactive Fallback через аннотации")
                .body("Reactive данные получены через fallback механизм. " +
                    "Внешний API временно недоступен.")
                .userId(0L)
                .build()
        );
    }

    /**
     * Комбинированное использование нескольких resilience patterns - АННОТАЦИОННЫЙ подход.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод демонстрирует мощь аннотационного подхода - возможность легко
     * комбинировать несколько resilience patterns:
     * </p>
     * <ul>
     *   <li>@CircuitBreaker - защита от каскадных сбоев</li>
     *   <li>@TimeLimiter - ограничение времени выполнения</li>
     *   <li>@Retry - повторные попытки при временных сбоях</li>
     * </ul>
     *
     * <p><strong>Порядок применения:</strong></p>
     * <ol>
     *   <li>Retry - внутренний слой (самый близкий к методу)</li>
     *   <li>TimeLimiter - средний слой</li>
     *   <li>CircuitBreaker - внешний слой</li>
     * </ol>
     *
     * @param id идентификатор поста
     * @return CompletableFuture с данными поста
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "externalApi", fallbackMethod = "getRobustPostFallback")
    @TimeLimiter(name = "externalApi")
    @Retry(name = "externalApi")
    public CompletableFuture<ApiResponse> getRobustPost(Long id) {
        log.debug("Robust запрос поста с ID: {} (комбинированные аннотации)", id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Увеличиваем вероятность ошибок для демонстрации retry
                if (ThreadLocalRandom.current().nextDouble() < 0.3) {
                    throw new ExternalServiceException("Временная ошибка для демонстрации retry");
                }
                
                simulateExternalApiCall("getRobustPost-" + id);
                
                ApiResponse response = webClient.get()
                    .uri("/posts/{id}", id)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
                
                log.debug("Успешно получен robust пост: {}", response.getId());
                return response;
            } catch (Exception e) {
                log.error("Ошибка в robust методе для поста {}: {}", id, e.getMessage());
                throw new ExternalServiceException("Robust метод ошибка", e);
            }
        });
    }

    /**
     * Fallback для комбинированного метода.
     *
     * @param id идентификатор поста
     * @param ex исключение
     * @return CompletableFuture с fallback данными
     */
    public CompletableFuture<ApiResponse> getRobustPostFallback(Long id, Exception ex) {
        log.warn("Robust fallback для поста {} (комбинированные аннотации): {}",
            id, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        
        return CompletableFuture.completedFuture(
            ApiResponse.builder()
                .id(id)
                .title("Robust Fallback")
                .body("Данные получены через robust fallback после применения " +
                    "Circuit Breaker + TimeLimiter + Retry. Все попытки исчерпаны.")
                .userId(0L)
                .build()
        );
    }

    // ===============================
    // ОСТАЛЬНЫЕ МЕТОДЫ
    // ===============================

    /**
     * Получение списка всех постов с Circuit Breaker (асинхронно).
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Для операций, возвращающих коллекции, важно предоставлять разумные fallback
     * значения. Пустой список часто лучше, чем исключение для пользовательского опыта.
     * </p>
     *
     * @return Flux со списком постов
     */
    public Flux<ApiResponse> getAllPostsAsync() {
        log.debug("Запрос всех постов");

        return webClient.get()
            .uri("/posts")
            .retrieve()
            .bodyToFlux(ApiResponse.class)
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .timeout(Duration.ofSeconds(10))
            .doOnError(throwable -> {
                log.error("Ошибка при получении списка постов: {}", throwable.getMessage());
            })
            .onErrorResume(throwable -> {
                log.warn("Использование fallback списка постов: {}", throwable.getMessage());
                return Flux.fromIterable(createFallbackPosts());
            })
            .doOnComplete(() -> {
                log.debug("Успешно получен список постов");
            });
    }

    /**
     * Демонстрация работы с Circuit Breaker в разных состояниях.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод позволяет протестировать различные сценарии работы Circuit Breaker:
     * успех, ошибка, timeout. Полезно для понимания поведения в разных ситуациях.
     * </p>
     *
     * @param scenario тип сценария: "success", "error", "timeout"
     * @return результат выполнения сценария
     */
    public Mono<String> testCircuitBreakerScenario(String scenario) {
        log.info("Тестирование сценария Circuit Breaker: {}", scenario);

        return Mono.fromSupplier(() -> {
                switch (scenario.toLowerCase()) {
                    case "success":
                        simulateExternalApiCall("success");
                        return "Успешное выполнение операции внешнего API";
                    case "error":
                        throw new ExternalServiceException("Симуляция ошибки внешнего API сервиса");
                    case "timeout":
                        try {
                            Thread.sleep(6000); // больше timeout Circuit Breaker
                            return "Этот результат не должен вернуться";
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new ExternalServiceException("Прервано во время ожидания", e);
                        }
                    default:
                        throw new IllegalArgumentException("Неизвестный сценарий: " + scenario);
                }
            })
            .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(throwable -> {
                log.warn("Fallback для сценария {}: {}", scenario, throwable.getMessage());
                return Mono.just("Fallback результат для сценария: " + scenario);
            });
    }

    /**
     * Проверка доступности внешнего API.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Health check для внешних API помогает определить доступность сервиса
     * и может использоваться для принятия решений о маршрутизации.
     * Fallback обрабатывается через try-catch, так как executeSupplier не имеет recover().
     * </p>
     *
     * @return true если внешний API доступен
     */
    public boolean isExternalApiHealthy() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                simulateExternalApiCall("healthCheck");
                return true;
            });
        } catch (Exception e) {
            log.warn("External API health check failed: {}",
                e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Создание fallback поста для graceful degradation.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Хорошие fallback данные должны быть meaningful для пользователя.
     * Избегайте null значений и предоставляйте информативные сообщения.
     * </p>
     *
     * @param id идентификатор поста
     * @return fallback пост
     */
    private ApiResponse createFallbackPost(Long id) {
        return ApiResponse.builder()
            .id(id)
            .title("Внешний сервис временно недоступен")
            .body("К сожалению, внешний API временно недоступен. " +
                "Попробуйте обновить страницу через несколько минут.")
            .userId(0L)
            .build();
    }

    /**
     * Создание fallback списка постов.
     *
     * @return список fallback постов
     */
    private List<ApiResponse> createFallbackPosts() {
        return Arrays.asList(
            createFallbackPost(1L),
            ApiResponse.builder()
                .id(2L)
                .title("Кэшированные данные")
                .body("Отображаются последние кэшированные данные из внешнего API.")
                .userId(0L)
                .build()
        );
    }

    /**
     * Имитация вызова внешнего API с возможными проблемами.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Этот метод симулирует реальные проблемы внешних API: network timeouts,
     * rate limiting, service unavailability. В реальном приложении такие проблемы
     * происходят непредсказуемо.
     * </p>
     *
     * @param operation название операции для логирования
     * @throws RuntimeException при симуляции ошибок внешнего API
     */
    private void simulateExternalApiCall(String operation) {
        try {
            // Симуляция нормального времени выполнения HTTP запроса (100-300ms)
            int delay = ThreadLocalRandom.current().nextInt(100, 300);
            Thread.sleep(delay);

            // Симуляция случайных ошибок внешнего API (7% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.07) {
                // Различные типы ошибок внешних API
                double errorType = ThreadLocalRandom.current().nextDouble();

                if (errorType < 0.3) {
                    throw new RuntimeException("External API connection timeout");
                } else if (errorType < 0.6) {
                    throw new RuntimeException("External API rate limit exceeded");
                } else if (errorType < 0.8) {
                    throw new RuntimeException("External API service unavailable (503)");
                } else {
                    throw new RuntimeException("External API authentication failed (401)");
                }
            }

            // Симуляция медленных ответов от внешнего API (3% вероятность)
            if (ThreadLocalRandom.current().nextDouble() < 0.03) {
                log.warn("Медленный ответ от внешнего API для операции: {} (> 5s)", operation);
                Thread.sleep(6000); // больше timeout Circuit Breaker
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("External API call interrupted: " + operation, e);
        }
    }
}