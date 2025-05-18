package org.gualsh.demo.micromet.scheduler;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.micromet.service.OrderProcessingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Компонент для выполнения периодических задач и демонстрации метрик для фоновых процессов.
 * <p>
 * Этот класс имитирует фоновую работу системы, генерируя заказы и выполняя обслуживание.
 * Он демонстрирует, как мониторить фоновые процессы, используя Micrometer.
 * </p>
 */
@Component
@Slf4j
public class DemoScheduler {

    private final OrderProcessingService orderProcessingService;
    private final MeterRegistry meterRegistry;
    private final Random random = new Random();
    private final Counter scheduledTasksCounter;
    private final Timer maintenanceTaskTimer;
    private final String[] regions = {"us", "eu", "asia", "africa"};

    /**
     * Создает новый экземпляр планировщика с настройкой метрик.
     *
     * @param orderProcessingService сервис обработки заказов
     * @param meterRegistry регистр метрик
     */
    public DemoScheduler(OrderProcessingService orderProcessingService, MeterRegistry meterRegistry) {
        this.orderProcessingService = orderProcessingService;
        this.meterRegistry = meterRegistry;

        // Инициализация метрик для отслеживания запланированных задач
        this.scheduledTasksCounter = Counter.builder("scheduler.tasks.executed")
            .description("Count of scheduler task executions")
            .register(meterRegistry);

        this.maintenanceTaskTimer = Timer.builder("scheduler.maintenance.time")
            .description("Time spent on maintenance tasks")
            .publishPercentiles(0.5, 0.95)
            .register(meterRegistry);

        log.info("Demo scheduler initialized");
    }

    /**
     * Запланированная задача, генерирующая заказы.
     * Выполняется каждые 5 секунд.
     */
    @Scheduled(fixedRate = 5000)
    public void generateOrdersBatch() {
        // Увеличиваем счетчик выполнения задач
        scheduledTasksCounter.increment();

        // Генерируем пакет заказов
        int batchSize = 1 + random.nextInt(3);
        log.info("Generating {} orders batch", batchSize);

        for (int i = 0; i < batchSize; i++) {
            String orderId = UUID.randomUUID().toString();
            double amount = 100.0 + random.nextDouble() * 900.0;
            String region = regions[random.nextInt(regions.length)];

            // Обрабатываем заказ через сервис
            boolean success = orderProcessingService.processOrder(orderId, amount, region);
            log.debug("Generated order: {}, success: {}", orderId, success);
        }
    }

    /**
     * Задача обслуживания, выполняемая каждую минуту.
     * Демонстрирует использование Timer для длительных задач.
     */
    @Scheduled(fixedRate = 60000)
    public void performMaintenance() {
        log.info("Starting maintenance task");

        // Отслеживаем время выполнения задачи
        maintenanceTaskTimer.record(() -> {
            try {
                // Имитируем длительную операцию обслуживания
                int maintenanceTime = 2000 + random.nextInt(3000);
                TimeUnit.MILLISECONDS.sleep(maintenanceTime);

                log.info("Maintenance completed in {} ms", maintenanceTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Maintenance task interrupted");
            }
        });
    }
}