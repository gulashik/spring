package org.gualsh.demo.micromet.controller.showmetrics;

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
 * REST контроллер для демонстрации трассировки с использованием Micrometer Observation API.
 * <p>
 * Этот контроллер предоставляет эндпоинты, демонстрирующие:
 * <ul>
 *   <li>Базовую трассировку запросов с помощью Micrometer</li>
 *   <li>Создание пользовательских наблюдений (observations) для отслеживания цепочки вызовов</li>
 *   <li>Добавление метаданных к трассировке через теги высокой и низкой кардинальности</li>
 *   <li>Управление контекстом трассировки при генерации ошибок</li>
 * </ul>
 * </p>
 * <p>
 * Подход с использованием Micrometer Observation API обеспечивает:
 * <ul>
 *   <li>Единый интерфейс для различных систем трассировки (Zipkin, Jaeger и др.)</li>
 *   <li>Автоматическую интеграцию с Spring WebMVC для эндпоинтов</li>
 *   <li>Возможность расширения и настройки через наблюдатели (listeners)</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/tracing")
@Slf4j
public class TracingController {

    /**
     * Сервис для демонстрации различных сценариев трассировки.
     * Предоставляет методы для создания и управления трассировкой в различных ситуациях.
     */
    private final TracingDemo tracingDemo;
    
    /**
     * Реестр наблюдений Micrometer, центральный компонент для создания и управления
     * трассировкой запросов и пользовательских операций.
     * <p>
     * Этот реестр автоматически интегрируется с настроенными системами трассировки
     * (например, Zipkin, Jaeger) и метрик (Prometheus).
     * </p>
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Создает новый экземпляр контроллера трассировки.
     *
     * @param tracingDemo Демонстрационный сервис трассировки, обеспечивающий
     *                    базовую функциональность для примеров трассировки.
     * @param observationRegistry Реестр наблюдений Micrometer, используемый для
     *                           создания и регистрации наблюдений в цепочке трассировки.
     */
    public TracingController(TracingDemo tracingDemo, ObservationRegistry observationRegistry) {
        this.tracingDemo = tracingDemo;
        this.observationRegistry = observationRegistry;
        log.info("TracingController инициализирован с реестром наблюдений");
    }

    /**
     * Выполняет демонстрационную операцию с полной трассировкой.
     * <p>
     * Этот эндпоинт запускает последовательность операций с использованием
     * сервиса {@link TracingDemo}, который создает цепочку вызовов
     * с поддержкой трассировки. Запрос автоматически включается в существующую
     * трассировку благодаря интеграции Spring MVC с Micrometer.
     * </p>
     *
     * @return ResponseEntity с результатом операции и временной меткой в формате Map
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> runTracingDemo() {
        log.info("Запуск демонстрационного эндпоинта трассировки");

        // Выполняем демонстрационный сценарий трассировки
        String result = tracingDemo.demoTrace();
        log.debug("Получен результат трассировки: {}", result);

        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет пользовательскую операцию с трассировкой и указанным именем.
     * <p>
     * Этот эндпоинт демонстрирует, как можно добавить дополнительный контекст
     * к существующей трассировке с помощью сервиса {@link TracingDemo}. Генерируется
     * уникальный ID для каждой операции, который может использоваться для корреляции
     * в системе трассировки.
     * </p>
     *
     * @param operationName Имя операции для трассировки. Это значение используется
     *                     как метаданные в системе трассировки для идентификации
     *                     операции. По умолчанию "custom-operation".
     * @return ResponseEntity с именем операции, её ID и результатом в формате Map
     */
    @GetMapping("/operation")
    public ResponseEntity<Map<String, Object>> customOperation(
        @RequestParam(defaultValue = "custom-operation") String operationName) {

        log.info("Выполнение пользовательской операции: {}", operationName);

        // Генерируем уникальный ID для операции
        String operationId = UUID.randomUUID().toString();
        log.debug("Сгенерирован ID операции: {}", operationId);

        // Выполняем операцию с трассировкой
        String result = tracingDemo.performTracedOperation(operationId);

        Map<String, Object> response = new HashMap<>();
        response.put("operation_name", operationName);
        response.put("operation_id", operationId);
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Демонстрирует программное создание трассировки с помощью Observation API.
     * <p>
     * Этот эндпоинт показывает, как создавать пользовательские наблюдения
     * (observations) непосредственно в коде, добавляя метаданные с помощью
     * тегов высокой и низкой кардинальности. Spring Boot автоматически создает
     * трейсы для контроллеров, но этот метод позволяет добавить дополнительный контекст.
     * </p>
     * <p>
     * Теги низкой кардинальности ({@code lowCardinalityKeyValue}) предназначены для
     * значений с небольшим количеством возможных вариантов и могут использоваться
     * для фильтрации в системах мониторинга.
     * </p>
     * <p>
     * Теги высокой кардинальности ({@code highCardinalityKeyValue}) подходят для
     * уникальных значений, таких как идентификаторы, и сохраняются только в системе
     * трассировки, но не в метриках.
     * </p>
     *
     * @param id Идентификатор ресурса, который будет добавлен как тег высокой 
     *          кардинальности в трассировку
     * @return ResponseEntity с данными запрошенного ресурса в формате Map
     */
    @GetMapping("/resource/{id}")
    public ResponseEntity<Map<String, Object>> getResource(@PathVariable String id) {
        log.info("Получение ресурса с ID: {}", id);

        // Демонстрация программного создания Observation с дополнительным контекстом
        return Observation.createNotStarted("api.get.resource", observationRegistry)
            .lowCardinalityKeyValue("resource.type", "demo")
            .highCardinalityKeyValue("resource.id", id)
            .observe(() -> {
                log.debug("Выполнение операции получения ресурса в контексте наблюдения");

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
     * Демонстрирует создание и трассировку ошибок в системе наблюдений.
     * <p>
     * Этот эндпоинт намеренно генерирует исключение в контексте наблюдения,
     * что позволяет системе трассировки отслеживать ошибки и их причины.
     * Micrometer автоматически добавляет информацию об исключении в трассировку,
     * включая стек вызовов и сообщение об ошибке.
     * </p>
     * <p>
     * Это полезно для:
     * <ul>
     *   <li>Определения частоты и типов ошибок в системе</li>
     *   <li>Анализа причин сбоев в распределенных системах</li>
     *   <li>Корреляции ошибок с конкретными запросами пользователей</li>
     * </ul>
     * </p>
     *
     * @return Теоретически возвращает ResponseEntity с информацией об ошибке,
     *         но практически всегда выбрасывает исключение
     * @throws RuntimeException Демонстрационная ошибка для трассировки
     */
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> generateTracedError() {
        log.info("Генерация трассируемой ошибки для демонстрации");

        return Observation.createNotStarted("api.error.demo", observationRegistry)
            .lowCardinalityKeyValue("error.type", "demonstration")
            .observe(() -> {
                log.debug("Выполнение операции с ошибкой в контексте наблюдения");

                // Симулируем ошибку для демонстрации трассировки
                throw new RuntimeException("Демонстрационная ошибка для системы трассировки");
            });
    }
}