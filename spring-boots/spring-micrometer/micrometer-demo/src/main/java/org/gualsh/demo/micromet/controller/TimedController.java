package org.gualsh.demo.micromet.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.micromet.service.OrderProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * REST контроллер демонстрируют
 * различные способы измерения производительности, использование таймеров.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class TimedController {

    private final MeterRegistry meterRegistry;
    private final OrderProcessingService orderProcessingService;

    private final Random random = new Random();
    private final String[] regions = {"us", "eu", "asia", "africa"};
    private final Timer resourcesTimer;

    public TimedController(MeterRegistry meterRegistry, OrderProcessingService orderProcessingService) {
        this.meterRegistry = meterRegistry;
        
        // todo Регистрируем Timer один раз при создании контроллера
        //  Создается с помощью билдера и регистрируется в MeterRegistry
        this.resourcesTimer = Timer.builder("api.resources.request")
            .description("Time taken to return resources info")
            .tag("method", "GET")
            .register(meterRegistry);

        this.orderProcessingService = orderProcessingService;
    }

    /**
     * Демонстрирует использование аннотации @Timed для автоматического измерения
     * времени выполнения метода.
     *
     * @return информацию о статусе сервера
     */
    @GetMapping("/status")
    // todo Требует регистрации бина TimedAspect определен в MicrometerConfig
    @Timed(
        value = "api.status.request",
        description = "Time taken to return status",
        percentiles = {0.5, 0.9, 0.95, 0.99} // todo Конфигурируется с параметрами для сбора процентилей (0.5, 0.9, 0.95, 0.99)

    )
    public ResponseEntity<Map<String, Object>> getStatus() {
        // Имитируем небольшую задержку для демонстрации метрик
        randomDelay(50);

        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("queue_size", orderProcessingService.getQueueSize());

        return ResponseEntity.ok(status);
    }

    /**
     * Демонстрирует ручное измерение времени выполнения с использованием Timer.
     *
     * @return информацию о системных ресурсах
     */
    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getResources() {
        // todo Используем Timer напрямую вместо аннотации
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Имитируем обработку
            randomDelay(100);

            Map<String, Object> resources = new HashMap<>();
            resources.put("cpu_usage", random.nextInt(100));
            resources.put("memory_used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            resources.put("memory_free", Runtime.getRuntime().freeMemory());
            resources.put("total_memory", Runtime.getRuntime().totalMemory());

            return ResponseEntity.ok(resources);
        } finally {
            // todo Обязательно останавливаем таймер и записываем результат
            sample.stop(resourcesTimer);
        }
    }

    /**
     * Обрабатывает заказ и возвращает результат.
     * Демонстрирует как обновлять бизнес-метрики.
     *
     * @param orderRequest данные заказа в JSON формате
     * @return результат обработки заказа
     */
    @PostMapping("/orders")
    @Timed("api.orders.creation")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        String orderId = UUID.randomUUID().toString();

        // Извлекаем данные из запроса или используем значения по умолчанию
        double amount = orderRequest.containsKey("amount")
            ? Double.parseDouble(orderRequest.get("amount").toString())
            : 100.0 + random.nextDouble() * 900.0;

        String region = orderRequest.containsKey("region")
            ? orderRequest.get("region").toString()
            : regions[random.nextInt(regions.length)];

        // Обрабатываем заказ через сервис, который обновляет метрики
        boolean success = orderProcessingService.processOrder(orderId, amount, region);

        Map<String, Object> response = new HashMap<>();
        response.put("order_id", orderId);
        response.put("status", success ? "success" : "failed");
        response.put("amount", amount);
        response.put("region", region);

        return ResponseEntity.ok(response);
    }

    /**
     * Генерирует ошибки для тестирования метрик ошибок.
     *
     * @param type тип ошибки для генерации
     * @return никогда не возвращается, так как метод всегда выбрасывает исключение
     */
    @GetMapping("/error/{type}")
    public ResponseEntity<String> generateError(@PathVariable String type) {
        log.info("Generating error of type: {}", type);

        switch (type) {
            case "runtime":
                throw new RuntimeException("Демонстрационная ошибка времени выполнения");
            case "illegal":
                throw new IllegalArgumentException("Демонстрационная ошибка неверного аргумента");
            case "nullpointer":
                // Симулируем NullPointerException
                String nullStr = null;
                return ResponseEntity.ok(nullStr.toLowerCase());
            default:
                throw new UnsupportedOperationException("Неизвестный тип ошибки: " + type);
        }
    }

    /**
     * Генерирует нагрузку для тестирования метрик.
     *
     * @param seconds длительность генерации нагрузки в секундах
     * @return результат операции
     */
    @GetMapping("/load-test")
    public ResponseEntity<Map<String, Object>> generateLoad(@RequestParam(defaultValue = "10") int seconds) {
        log.info("Generating load for {} seconds", seconds);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (seconds * 1000);
        int requestCount = 0;

        // Ограничиваем максимальное время работы в 60 секунд для безопасности
        seconds = Math.min(seconds, 60);

        while (System.currentTimeMillis() < endTime) {
            // Симулируем создание заказа
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("amount", 100.0 + random.nextDouble() * 900.0);
            orderData.put("region", regions[random.nextInt(regions.length)]);

            createOrder(orderData);
            requestCount++;

            // Небольшая пауза между запросами
            randomDelay(20);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("load_test", "completed");
        result.put("requests_processed", requestCount);
        result.put("duration_seconds", seconds);

        return ResponseEntity.ok(result);
    }

    /**
     * Добавляет случайную задержку для имитации времени обработки.
     *
     * @param baseMillis базовое время задержки в миллисекундах
     */
    private void randomDelay(int baseMillis) {
        try {
            // Базовая задержка плюс случайная составляющая
            TimeUnit.MILLISECONDS.sleep(baseMillis + random.nextInt(baseMillis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}