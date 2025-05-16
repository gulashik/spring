package org.gualsh.demo.micromet.service;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис для демонстрации различных аспектов работы с метриками.
 */
@Service
public class DemoService {

    private static final Logger logger = LoggerFactory.getLogger(DemoService.class);

    private final MeterRegistry meterRegistry;
    private final Random random = new Random();

    // Примеры различных типов метрик
    private final Counter taskCounter;
    private final Timer taskTimer;
    private final DistributionSummary taskSizeSummary;
    private final AtomicInteger activeTasks;
    private final Counter.Builder errorCounterBuilder;

    @Autowired
    public DemoService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Счетчик выполненных задач
        this.taskCounter = Counter.builder("task.count")
            .description("Общее количество выполненных задач")
            .register(meterRegistry);

        // Таймер для измерения продолжительности задач
        this.taskTimer = Timer.builder("task.duration")
            .description("Продолжительность выполнения задач")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        // Distribution Summary для измерения размера задач
        this.taskSizeSummary = DistributionSummary.builder("task.size")
            .description("Распределение размеров задач")
            .baseUnit("bytes")
            .publishPercentiles(0.5, 0.95)
            .register(meterRegistry);

        // Gauge для отслеживания активных задач
        this.activeTasks = new AtomicInteger(0);
        Gauge.builder("task.active", activeTasks::get)
            .description("Текущее количество активных задач")
            .register(meterRegistry);

        // Builder для счетчиков ошибок разных типов
        this.errorCounterBuilder = Counter.builder("task.errors")
            .description("Ошибки при выполнении задач");
    }

    /**
     * Выполняет задачу с указанной задержкой и фиксирует метрики.
     *
     * @param seconds Длительность задачи в секундах
     */
    public void executeTask(int seconds) {
        activeTasks.incrementAndGet();

        try {
            // Фиксируем размер задачи (в данном случае это условное значение)
            int taskSize = random.nextInt(1000) * seconds;
            taskSizeSummary.record(taskSize);

            // Фиксируем время выполнения задачи
            taskTimer.record(() -> {
                try {
                    TimeUnit.SECONDS.sleep(seconds);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCounterBuilder.tag("type", "interrupted").register(meterRegistry).increment();
                    throw new RuntimeException("Task interrupted", e);
                }
            });

            // Увеличиваем счетчик успешно выполненных задач
            taskCounter.increment();

        } finally {
            activeTasks.decrementAndGet();
        }
    }

    /**
     * Генерирует тестовую нагрузку на CPU.
     *
     * @param intensity Интенсивность нагрузки от 1 до 10
     */
    public void generateLoad(int intensity) {
        logger.info("Generating load with intensity {}", intensity);

        // Метрика для интенсивности
        meterRegistry.gauge("load.intensity", Tags.of("source", "manual"), intensity);

        // Создаем временную нагрузку
        Timer.Sample sample = Timer.start(meterRegistry);

        long endTime = System.currentTimeMillis() + (intensity * 1000L);
        while (System.currentTimeMillis() < endTime) {
            // Интенсивные вычисления для нагрузки на CPU
            for (int i = 0; i < intensity * 100_000; i++) {
                Math.sin(random.nextDouble() * Math.PI);
            }
        }

        sample.stop(meterRegistry.timer("load.duration", "intensity", String.valueOf(intensity)));
    }

    /**
     * Возвращает статистику о выполненных задачах.
     *
     * @return Карта со статистическими данными
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("tasksCompleted", taskCounter.count());
        stats.put("averageTaskDuration", taskTimer.mean(TimeUnit.MILLISECONDS));
        stats.put("maxTaskDuration", taskTimer.max(TimeUnit.MILLISECONDS));
        stats.put("activeTasks", activeTasks.get());
        stats.put("medianTaskSize", taskSizeSummary.percentile(0.5));

        return stats;
    }

    /**
     * Запланированная задача для генерации метрик с фиксированной частотой.
     * Выполняется каждые 30 секунд.
     */
    @Scheduled(fixedRate = 30000)
    public void generatePeriodicMetrics() {
        logger.debug("Generating periodic metrics");

        // Генерируем случайные метрики для демонстрации
        int randomTasksCount = random.nextInt(10);

        // Метрика периодической активности
        meterRegistry.counter("periodic.activity").increment();

        // Случайный размер порции данных
        meterRegistry.summary("periodic.batch.size").record(random.nextDouble() * 1024);

        for (int i = 0; i < randomTasksCount; i++) {
            int taskDuration = random.nextInt(500);
            meterRegistry.timer("periodic.task.duration")
                .record(taskDuration, TimeUnit.MILLISECONDS);

            boolean success = random.nextDouble() > 0.2; // 20% шанс ошибки
            meterRegistry.counter("periodic.task.result",
                "success", String.valueOf(success)).increment();
        }
    }
}