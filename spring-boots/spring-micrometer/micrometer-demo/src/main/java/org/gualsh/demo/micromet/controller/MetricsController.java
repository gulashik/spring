package org.gualsh.demo.micromet.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST контроллер для демонстрации взаимодействия с метриками через API.
 * <p>
 * Этот контроллер предоставляет эндпоинты для получения информации о доступных
 * метриках в приложении и их значениях.
 * </p>
 */
@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final MetricsEndpoint metricsEndpoint;

    /**
     * Создает новый экземпляр контроллера метрик.
     *
     * @param meterRegistry регистр метрик
     * @param metricsEndpoint эндпоинт метрик Spring Boot Actuator
     */
    public MetricsController(MeterRegistry meterRegistry, MetricsEndpoint metricsEndpoint) {
        this.meterRegistry = meterRegistry;
        this.metricsEndpoint = metricsEndpoint;
    }

    /**
     * Получает список всех доступных метрик в приложении.
     *
     * @return список имен метрик
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listMetrics() {
        log.info("Listing all available metrics");

        // Получаем все имена метрик из эндпоинта Actuator
        Set<String> metricNames = new TreeSet<>(metricsEndpoint.listNames().getNames());

        Map<String, Object> response = new HashMap<>();
        response.put("metrics_count", metricNames.size());
        response.put("metrics", metricNames);

        return ResponseEntity.ok(response);
    }

    /**
     * Получает список метрик, отфильтрованных по префиксу имени.
     *
     * @param prefix префикс для фильтрации метрик (например, "jvm", "http", etc.)
     * @return отфильтрованный список метрик
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMetrics(
        @RequestParam(defaultValue = "") String prefix) {
        log.info("Searching metrics with prefix: {}", prefix);

        // Ищем метрики с указанным префиксом
        Set<String> metricNames = meterRegistry.getMeters().stream()
            .map(meter -> meter.getId().getName())
            .filter(name -> name.startsWith(prefix))
            .sorted()
            .collect(Collectors.toCollection(TreeSet::new));

        Map<String, Object> response = new HashMap<>();
        response.put("prefix", prefix);
        response.put("metrics_count", metricNames.size());
        response.put("metrics", metricNames);

        return ResponseEntity.ok(response);
    }

    /**
     * Получает детальную информацию о конкретной метрике.
     *
     * @param name имя метрики
     * @return детальная информация о метрике или ошибка, если метрика не найдена
     */
    @GetMapping("/info")
    public ResponseEntity<Object> getMetricInfo(@RequestParam String name) {
        log.info("Getting info for metric: {}", name);

        // Получаем метрику и возвращаем её напрямую
        var metric = metricsEndpoint.metric(name, null);

        if (metric == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Metric not found");
            error.put("name", name);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metric);
    }

    /**
     * Получает сводную информацию о метриках по категориям.
     *
     * @return структурированная информация о метриках
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        log.info("Generating metrics summary");

        Map<String, Object> summary = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        // Группируем метрики по категориям на основе префикса имени
        meterRegistry.getMeters().stream()
            .map(meter -> meter.getId().getName())
            .forEach(name -> {
                String category = name.contains(".")
                    ? name.substring(0, name.indexOf('.'))
                    : "other";
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            });

        // Добавляем категории и количества в ответ
        summary.put("total_metrics", meterRegistry.getMeters().size());
        summary.put("categories", categoryCounts);

        // Добавляем примеры метрик для каждой категории
        Map<String, List<String>> categoryExamples = new HashMap<>();

        categoryCounts.keySet().forEach(category -> {
            String prefix = category.equals("other") ? "" : category + ".";

            List<String> examples = meterRegistry.getMeters().stream()
                .map(meter -> meter.getId().getName())
                .filter(name -> name.startsWith(prefix))
                .limit(5)  // Ограничиваем количество примеров
                .collect(Collectors.toList());

            categoryExamples.put(category, examples);
        });

        summary.put("examples", categoryExamples);

        return ResponseEntity.ok(summary);
    }
}