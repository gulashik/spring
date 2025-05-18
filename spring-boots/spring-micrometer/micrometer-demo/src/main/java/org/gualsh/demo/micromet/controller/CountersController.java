package org.gualsh.demo.micromet.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * REST контроллер с демонстрацией различных счетчиков и способов применения метрик.
 * <p>
 * Этот контроллер показывает, как создавать и использовать различные типы счетчиков
 * и метрик в бизнес-логике приложения.
 * </p>
 */
@RestController
@RequestMapping("/api/counters")
@Slf4j
public class CountersController {

    /**
     * Основной регистр метрик Micrometer.
     * Используется для регистрации и управления всеми счетчиками и метриками.
     */
    private final MeterRegistry meterRegistry;
    
    /**
     * Генератор случайных чисел для имитации непредсказуемого поведения в методах.
     * Используется для симуляции ошибок и вариативности времени выполнения операций.
     */
    private final Random random = new Random();

    /**
     * Счетчик успешных операций.
     * Инкрементируется каждый раз, когда операция завершается успешно.
     */
    private final Counter successCounter;
    
    /**
     * Счетчик неудачных операций.
     * Инкрементируется при возникновении ошибок во время выполнения операций.
     */
    private final Counter failureCounter;
    
    /**
     * Таймер для измерения длительности операций.
     * Собирает статистику по времени выполнения операций, включая процентили.
     */
    private final Timer operationTimer;
    
    /**
     * Распределение значений операций.
     * Собирает статистику по значениям, переданным в операции.
     */
    private final DistributionSummary valueSummary;
    
    /**
     * Счетчик активных запросов.
     * Отслеживает количество одновременно обрабатываемых запросов.
     * Используется атомарный тип для обеспечения потокобезопасности.
     */
    private final AtomicInteger activeRequests = new AtomicInteger(0);

    /**
     * Создает новый экземпляр контроллера со счетчиками.
     * Инициализирует все необходимые счетчики и метрики через MeterRegistry.
     *
     * @param meterRegistry регистр метрик для регистрации счетчиков
     */
    public CountersController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Инициализируем счетчики
        this.successCounter = Counter.builder("api.counters.success")
            .description("Number of successful operations")
            .register(meterRegistry);

        this.failureCounter = Counter.builder("api.counters.failure")
            .description("Number of failed operations")
            .register(meterRegistry);

        this.operationTimer = Timer.builder("api.counters.operation.timer")
            .description("Time spent on operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        this.valueSummary = DistributionSummary.builder("api.counters.value.summary")
            .description("Distribution of operation values")
            .publishPercentiles(0.5, 0.75, 0.95)
            .register(meterRegistry);

        // Регистрируем gauge для отслеживания активных запросов
        meterRegistry.gauge("api.counters.active.requests", activeRequests);

        log.info("CountersController initialized with metrics");
    }

    /**
     * Выполняет операцию и инкрементирует соответствующие счетчики.
     * Демонстрирует ручную работу со счетчиками и таймерами Micrometer.
     * <p>
     * Этот метод показывает комплексный сценарий использования нескольких метрик:
     * - Измерение времени выполнения операции через Timer
     * - Учет распределения значений через DistributionSummary
     * - Подсчет успешных и неудачных операций
     * - Отслеживание количества активных запросов через Gauge
     * </p>
     *
     * @param value значение для операции, которое влияет на время обработки и записывается в распределение
     * @return ResponseEntity с результатом операции или информацией об ошибке в формате Map
     */
    @PostMapping("/operation")
    public ResponseEntity<Map<String, Object>> performOperation(@RequestParam(defaultValue = "10") int value) {
        log.info("Performing operation with value: {}", value);

        // Увеличиваем счетчик активных запросов
        activeRequests.incrementAndGet();

        try {
            // Записываем значение в distribution summary
            valueSummary.record(value);

            // Используем таймер для измерения времени операции
            return operationTimer.record(() -> {
                try {
                    // Имитируем выполнение операции
                    TimeUnit.MILLISECONDS.sleep(value * 10);

                    // Имитируем случайные ошибки
                    if (random.nextInt(10) == 0) {
                        throw new RuntimeException("Simulated failure");
                    }

                    // Увеличиваем счетчик успешных операций
                    successCounter.increment();

                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "success");
                    result.put("value", value);
                    result.put("processingTime", value * 10);

                    return ResponseEntity.ok(result);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failureCounter.increment();

                    Map<String, Object> error = new HashMap<>();
                    error.put("status", "error");
                    error.put("message", "Operation interrupted");

                    return ResponseEntity.internalServerError().body(error);
                } catch (Exception e) {
                    failureCounter.increment();

                    Map<String, Object> error = new HashMap<>();
                    error.put("status", "error");
                    error.put("message", e.getMessage());

                    return ResponseEntity.internalServerError().body(error);
                }
            });
        } finally {
            // Всегда уменьшаем счетчик активных запросов
            activeRequests.decrementAndGet();
        }
    }

    /**
     * Демонстрирует использование аннотации @Counted для автоматического
     * инкрементирования счетчика.
     * <p>
     * В отличие от ручного управления счетчиками, этот метод показывает
     * декларативный подход к метрикам с использованием аннотаций Micrometer.
     * Каждый вызов метода будет автоматически увеличивать счетчик.
     * </p>
     *
     * @param type тип запроса, который включается в ответ и используется в логах
     * @return ResponseEntity с информацией о типе запроса и временной метке в формате Map
     */
    @GetMapping("/count/{type}")
    @Counted(value = "api.counters.annotated.count", extraTags = {"method", "count"})
    public ResponseEntity<Map<String, Object>> countRequest(@PathVariable String type) {
        log.info("Counting request of type: {}", type);

        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Демонстрирует комбинирование аннотаций @Timed и @Counted.
     * <p>
     * Этот метод показывает, как можно одновременно считать количество вызовов 
     * и измерять время выполнения с помощью декларативных аннотаций.
     * Обе метрики будут автоматически регистрироваться в MeterRegistry.
     * </p>
     *
     * @param parameter параметр для обработки, используется в логах и включается в ответ
     * @return ResponseEntity с результатом обработки, включая переданный параметр и временную метку
     */
    @GetMapping("/process")
    @Timed(value = "api.counters.process.time",
        description = "Time spent processing request",
        percentiles = {0.5, 0.95, 0.99})
    @Counted(value = "api.counters.process.count",
        description = "Number of process requests")
    public ResponseEntity<Map<String, Object>> processRequest(
        @RequestParam(defaultValue = "default") String parameter) {
        log.info("Processing request with parameter: {}", parameter);

        // Имитируем обработку
        try {
            TimeUnit.MILLISECONDS.sleep(200 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("parameter", parameter);
        result.put("processed", true);
        result.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(result);
    }

    /**
     * Демонстрирует создание счетчика "на лету" для конкретного запроса.
     * <p>
     * Этот метод показывает, как можно динамически создавать метрики в зависимости
     * от параметров запроса. Для каждой уникальной категории будет создан
     * отдельный экземпляр счетчика с соответствующим тегом категории.
     * </p>
     * <p>
     * Такой подход полезен, когда необходимо отслеживать метрики для динамического
     * набора сущностей или категорий, которые заранее неизвестны.
     * </p>
     *
     * @param category категория запроса, используется как тег в счетчике и включается в ответ
     * @return ResponseEntity с информацией о созданном счетчике и его категории
     */
    @PostMapping("/dynamic/{category}")
    public ResponseEntity<Map<String, Object>> createDynamicCounter(@PathVariable String category) {
        log.info("Creating dynamic counter for category: {}", category);

        // Создаем счетчик динамически на основе параметра
        Counter dynamicCounter = Counter.builder("api.counters.dynamic")
            .tag("category", category)
            .description("Dynamically created counter")
            .register(meterRegistry);

        // Инкрементируем счетчик
        dynamicCounter.increment();

        Map<String, Object> response = new HashMap<>();
        response.put("counter_name", "api.counters.dynamic");
        response.put("category", category);
        response.put("created", true);

        return ResponseEntity.ok(response);
    }
}