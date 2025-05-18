package org.gualsh.demo.micromet.controller;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.micromet.tracing.TracingDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST контроллер для демонстрации трассировки с Micrometer Observation API.
 * <p>
 * Этот контроллер предоставляет эндпоинты, демонстрирующие работу с трассировкой
 * и как создавать наблюдения (observations) для отслеживания цепочки вызовов.
 * </p>
 */
@RestController
@RequestMapping("/api/tracing")
@Slf4j
public class TracingController {

    private final TracingDemo tracingDemo;
    private final ObservationRegistry observationRegistry;

    /**
     * Создает новый экземпляр контроллера для трассировки.
     *
     * @param tracingDemo демонстрационный сервис трассировки
     * @param observationRegistry реестр наблюдений
     */
    public TracingController(TracingDemo tracingDemo, ObservationRegistry observationRegistry) {
        this.tracingDemo = tracingDemo;
        this.observationRegistry = observationRegistry;
    }

    /**
     * Выполняет демонстрационную операцию с трассировкой.
     *
     * @return результат операции
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> runTracingDemo() {
        log.info("Starting tracing demo endpoint");

        // Выполняем демонстрационный сценарий трассировки
        String result = tracingDemo.demoTrace();

        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет пользовательскую операцию с трассировкой.
     *
     * @param operationName имя операции для трассировки
     * @return результат операции
     */
    @GetMapping("/operation")
    public ResponseEntity<Map<String, Object>> customOperation(
        @RequestParam(defaultValue = "custom-operation") String operationName) {

        // Генерируем уникальный ID для операции
        String operationId = UUID.randomUUID().toString();

        // Выполняем операцию с трассировкой
        String result = tracingDemo.performTracedOperation(operationId);

        Map<String, Object> response = new HashMap<>();
        response.put("operation_name", operationName);
        response.put("operation_id", operationId);
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Демонстрирует трассировку с помощью аннотаций и AspectJ.
     * Spring Boot автоматически создает трейсы для контроллеров,
     * но можно добавить дополнительный контекст.
     *
     * @param id идентификатор ресурса
     * @return данные ресурса
     */
    @GetMapping("/resource/{id}")
    public ResponseEntity<Map<String, Object>> getResource(@PathVariable String id) {
        // Наблюдение создается автоматически для контроллера,
        // но мы можем добавить дополнительную информацию с помощью API

        return Observation.createNotStarted("api.get.resource", observationRegistry)
            .lowCardinalityKeyValue("resource.type", "demo")
            .highCardinalityKeyValue("resource.id", id)
            .observe(() -> {
                log.info("Getting resource: {}", id);

                // Здесь был бы код получения ресурса из базы данных или другого сервиса
                // Создаём демонстрационный ответ
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", id);
                resource.put("name", "Resource " + id);
                resource.put("created", System.currentTimeMillis());

                return ResponseEntity.ok(resource);
            });
    }

    /**
     * Демонстрирует создание ошибки для отслеживания в системе трассировки.
     *
     * @return никогда не возвращает успешный ответ
     */
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> generateTracedError() {
        return Observation.createNotStarted("api.error.demo", observationRegistry)
            .lowCardinalityKeyValue("error.type", "demonstration")
            .observe(() -> {
                log.info("Generating traced error for demonstration");

                // Симулируем ошибку
                throw new RuntimeException("Demonstration error for tracing");
            });
    }
}