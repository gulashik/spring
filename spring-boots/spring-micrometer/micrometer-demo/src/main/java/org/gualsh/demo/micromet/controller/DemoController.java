package org.gualsh.demo.micromet.controller;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import org.gualsh.demo.micromet.service.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Демонстрационный контроллер с различными эндпоинтами для тестирования метрик.
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private final DemoService demoService;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();

    // Пример создания собственных метрик напрямую
    private final Counter apiCallCounter;
    private final Timer apiCallTimer;

    @Autowired
    public DemoController(DemoService demoService, MeterRegistry meterRegistry) {
        this.demoService = demoService;
        this.meterRegistry = meterRegistry;

        // Инициализация собственных метрик
        this.apiCallCounter = Counter.builder("api.calls.total")
            .description("Общее количество вызовов API")
            .tag("controller", "DemoController")
            .register(meterRegistry);

        this.apiCallTimer = Timer.builder("api.calls.duration")
            .description("Время выполнения запросов API")
            .tag("controller", "DemoController")
            .publishPercentiles(0.5, 0.95, 0.99) // Публикуем перцентили
            .register(meterRegistry);
    }

    /**
     * Базовый эндпоинт с использованием аннотации @Timed для измерения времени выполнения.
     *
     * @return Простое текстовое приветствие
     */
    @GetMapping("/hello")
    @Timed(value = "api.hello.time", description = "Время обработки запроса /api/hello", percentiles = {0.5, 0.95, 0.99})
    public String hello() {
        apiCallCounter.increment(); // Инкрементируем счетчик вызовов API

        return apiCallTimer.record(() -> {
            try {
                // Имитация рабочей нагрузки
                TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Hello from Micrometer and Actuator Demo!";
        });
    }

    /**
     * Эндпоинт, демонстрирующий аннотацию @Counted для подсчета вызовов.
     *
     * @return Ответ со статистикой обработки
     */
    @GetMapping("/stats")
    @Counted(value = "api.stats.count", description = "Количество запросов к /api/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        apiCallCounter.increment();

        logger.info("Stats endpoint called");

        // Получаем статистику из сервиса
        Map<String, Object> stats = demoService.getStatistics();

        return ResponseEntity.ok(stats);
    }

    /**
     * Эндпоинт, демонстрирующий использование Observation API (@Observed).
     *
     * @param seconds Количество секунд задержки
     * @return Ответ о выполнении задачи
     */
    @GetMapping("/task/{seconds}")
    @Observed(name = "api.task.observation",
        contextualName = "task-execution",
        lowCardinalityKeyValues = {"api", "task"})
    public ResponseEntity<String> executeTask(@PathVariable int seconds) {
        apiCallCounter.increment();

        if (seconds > 10) {
            logger.warn("Long task requested: {} seconds", seconds);
            return ResponseEntity.badRequest().body("Task too long, maximum 10 seconds");
        }

        logger.info("Executing task with {} seconds delay", seconds);
        demoService.executeTask(seconds);

        return ResponseEntity.ok("Task completed after " + seconds + " seconds");
    }

    /**
     * Эндпоинт для демонстрации обработки ошибок и метрик ошибок.
     *
     * @param shouldFail Флаг для симуляции ошибки
     * @return Ответ об успешном выполнении или ошибка
     */
    @GetMapping("/test-error")
    @Timed(value = "api.error.time", description = "Время обработки запроса с возможной ошибкой")
    public ResponseEntity<String> testError(@RequestParam(defaultValue = "false") boolean shouldFail) {
        apiCallCounter.increment();

        // Отдельный счетчик для отслеживания запросов с потенциальными ошибками
        meterRegistry.counter("api.error.attempts", "shouldFail", String.valueOf(shouldFail)).increment();

        logger.info("Test error endpoint called with shouldFail={}", shouldFail);

        if (shouldFail) {
            // Метрика для отслеживания ошибок увеличивается здесь
            meterRegistry.counter("api.error.actual", "type", "intentional").increment();
            throw new RuntimeException("Test error triggered");
        }

        return ResponseEntity.ok("No error occurred");
    }

    /**
     * Эндпоинт для генерации случайной загрузки процессора.
     *
     * @param intensity Интенсивность нагрузки (1-10)
     * @return Сообщение о выполнении
     */
    @PostMapping("/load")
    public ResponseEntity<String> generateLoad(@RequestParam(defaultValue = "5") int intensity) {
        apiCallCounter.increment();

        if (intensity < 1 || intensity > 10) {
            return ResponseEntity.badRequest().body("Intensity must be between 1 and 10");
        }

        logger.info("Generating CPU load with intensity: {}", intensity);

        // Метрика для отслеживания запросов на генерацию нагрузки
        meterRegistry.gauge("api.load.intensity", intensity);

        // Генерируем нагрузку
        demoService.generateLoad(intensity);

        return ResponseEntity.ok("Generated load with intensity " + intensity);
    }
}