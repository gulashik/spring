package org.gualsh.demo.micromet.controller.showmetrics;

import io.micrometer.core.instrument.MeterRegistry;
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
 * метриках в приложении и их значениях. Контроллер является частью демонстрационного
 * приложения, показывающего возможности библиотеки Micrometer для сбора и анализа
 * метрик в Spring-приложениях.
 * </p>
 * <p>
 * Основные функциональные возможности:
 * <ul>
 *   <li>Получение списка всех доступных метрик в приложении</li>
 *   <li>Поиск метрик по префиксу имени</li>
 *   <li>Получение подробной информации о конкретной метрике</li>
 *   <li>Генерация сводной информации о метриках по категориям</li>
 * </ul>
 * </p>
 * <p>
 * Контроллер использует {@link MeterRegistry} для доступа к низкоуровневому API Micrometer
 * и {@link MetricsEndpoint} для получения структурированной информации о метриках через
 * Spring Boot Actuator.
 * </p>
 * 
 * @see io.micrometer.core.instrument.MeterRegistry
 * @see org.springframework.boot.actuate.metrics.MetricsEndpoint
 */
@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {

    /**
     * Регистр метрик Micrometer.
     * <p>
     * Основной компонент Micrometer, предоставляющий доступ ко всем зарегистрированным
     * метрикам в приложении. Позволяет получать, фильтровать и искать метрики
     * с использованием низкоуровневого API.
     * </p>
     */
    private final MeterRegistry meterRegistry;
    
    /**
     * Эндпоинт метрик Spring Boot Actuator.
     * <p>
     * Предоставляет высокоуровневый доступ к метрикам через API Spring Boot Actuator.
     * Используется для получения структурированной информации о метриках в формате,
     * совместимом с Actuator.
     * </p>
     */
    private final MetricsEndpoint metricsEndpoint;

    /**
     * Создает новый экземпляр контроллера метрик.
     * <p>
     * Конструктор получает необходимые зависимости через механизм внедрения зависимостей Spring.
     * Обе зависимости автоматически регистрируются в контексте Spring Boot приложения
     * при подключении соответствующих стартеров.
     * </p>
     *
     * @param meterRegistry регистр метрик для доступа к низкоуровневому API Micrometer
     * @param metricsEndpoint эндпоинт метрик Spring Boot Actuator для структурированного
     *                        представления метрик
     */
    public MetricsController(MeterRegistry meterRegistry, MetricsEndpoint metricsEndpoint) {
        this.meterRegistry = meterRegistry;
        this.metricsEndpoint = metricsEndpoint;
    }

    /**
     * Получает список всех доступных метрик в приложении.
     * <p>
     * Эндпоинт возвращает полный список имен всех зарегистрированных метрик,
     * отсортированный в алфавитном порядке. Список получается через {@link MetricsEndpoint},
     * что обеспечивает совместимость с форматом Spring Boot Actuator.
     * </p>
     * <p>
     * Ответ содержит общее количество доступных метрик и их полный список.
     * </p>
     *
     * @return {@link ResponseEntity} с объектом, содержащим:
     *         <ul>
     *           <li>"metrics_count" - общее количество доступных метрик</li>
     *           <li>"metrics" - отсортированный список имён всех метрик</li>
     *         </ul>
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
     * <p>
     * Эндпоинт позволяет найти все метрики, имена которых начинаются с указанного префикса.
     * Это удобно для фильтрации метрик по категориям, например:
     * <ul>
     *   <li>"jvm." - для метрик JVM (память, GC, потоки и т.д.)</li>
     *   <li>"http." - для HTTP-метрик (запросы, ответы, таймауты и т.д.)</li>
     *   <li>"system." - для системных метрик (загрузка CPU, дисковое пространство и т.д.)</li>
     * </ul>
     * </p>
     * <p>
     * В отличие от метода {@link #listMetrics()}, этот метод использует низкоуровневый API
     * {@link MeterRegistry} для более гибкого поиска метрик.
     * </p>
     *
     * @param prefix префикс для фильтрации метрик (например, "jvm", "http", и т.д.).
     *               Если не указан, возвращает все метрики (эквивалентно пустому префиксу).
     * @return {@link ResponseEntity} с объектом, содержащим:
     *         <ul>
     *           <li>"prefix" - использованный префикс фильтрации</li>
     *           <li>"metrics_count" - количество найденных метрик</li>
     *           <li>"metrics" - отсортированный список имён отфильтрованных метрик</li>
     *         </ul>
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
     * <p>
     * Эндпоинт предоставляет подробную информацию о метрике, включая:
     * <ul>
     *   <li>Имя метрики</li>
     *   <li>Описание (если доступно)</li>
     *   <li>Теги (ключи и значения)</li>
     *   <li>Текущие значения</li>
     *   <li>Статистические данные (для таймеров и распределений)</li>
     * </ul>
     * </p>
     * <p>
     * Использует {@link MetricsEndpoint} для получения структурированного представления
     * метрики в формате, совместимом с Spring Boot Actuator.
     * </p>
     *
     * @param name имя метрики, для которой требуется получить информацию.
     *             Имя должно точно соответствовать существующей метрике.
     * @return {@link ResponseEntity} со структурированной информацией о метрике
     *         или ответ 404 Not Found, если метрика не найдена
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
     * <p>
     * Эндпоинт генерирует сводный отчет о всех доступных метриках,
     * группируя их по категориям на основе префиксов имен. Это позволяет
     * получить общее представление о структуре и составе метрик в приложении.
     * </p>
     * <p>
     * Для каждой категории предоставляется:
     * <ul>
     *   <li>Общее количество метрик в категории</li>
     *   <li>Список примеров метрик (до 5 метрик из каждой категории)</li>
     * </ul>
     * </p>
     * <p>
     * Метрики группируются по первому сегменту имени до точки. Например,
     * метрики "jvm.memory.used" и "jvm.gc.pause" попадут в категорию "jvm".
     * Метрики без точек в имени будут отнесены к категории "other".
     * </p>
     *
     * @return {@link ResponseEntity} с объектом, содержащим:
     *         <ul>
     *           <li>"total_metrics" - общее количество метрик в приложении</li>
     *           <li>"categories" - отображение категорий на количество метрик в них</li>
     *           <li>"examples" - отображение категорий на примеры метрик (до 5 для каждой категории)</li>
     *         </ul>
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