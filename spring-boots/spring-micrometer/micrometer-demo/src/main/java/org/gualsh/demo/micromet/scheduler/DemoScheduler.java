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
 * <p>
 * Основные функциональные возможности:
 * <ul>
 *   <li>Периодическая генерация тестовых заказов</li>
 *   <li>Выполнение имитации обслуживания системы</li>
 *   <li>Сбор метрик о выполнении задач с помощью Counter и Timer</li>
 * </ul>
 * </p>
 * 
 * @see org.gualsh.demo.micromet.service.OrderProcessingService
 * @see io.micrometer.core.instrument.MeterRegistry
 */
@Component
@Slf4j
public class DemoScheduler {

    /**
     * Сервис обработки заказов.
     * <p>
     * Используется для передачи сгенерированных заказов на обработку
     * и обновления соответствующих бизнес-метрик.
     * </p>
     */
    private final OrderProcessingService orderProcessingService;
    
    /**
     * Регистр метрик Micrometer.
     * <p>
     * Основной компонент Micrometer, предоставляющий доступ к метрикам.
     * Используется для регистрации счетчиков и таймеров.
     * </p>
     */
    private final MeterRegistry meterRegistry;
    
    /**
     * Генератор случайных чисел.
     * <p>
     * Используется для создания случайных параметров заказов и времени имитации задач.
     * </p>
     */
    private final Random random = new Random();
    
    /**
     * Счетчик выполнения плановых задач.
     * <p>
     * Метрика, которая подсчитывает общее количество запусков запланированных задач.
     * Помогает отслеживать работоспособность планировщика.
     * </p>
     */
    private final Counter scheduledTasksCounter;
    
    /**
     * Таймер для измерения времени выполнения задач обслуживания.
     * <p>
     * Метрика, отслеживающая продолжительность выполнения задачи обслуживания.
     * Позволяет контролировать производительность и выявлять аномалии.
     * </p>
     */
    private final Timer maintenanceTaskTimer;
    
    /**
     * Список регионов для генерации тестовых заказов.
     * <p>
     * Используется для имитации географического распределения заказов
     * и демонстрации сегментации метрик по регионам.
     * </p>
     */
    private final String[] regions = {"us", "eu", "asia", "africa"};

    /**
     * Создает новый экземпляр планировщика с настройкой метрик.
     * <p>
     * В конструкторе происходит инициализация и регистрация метрик:
     * <ul>
     *   <li>Счетчик для учета выполненных запланированных задач</li>
     *   <li>Таймер для измерения времени выполнения задач обслуживания</li>
     * </ul>
     * </p>
     *
     * @param orderProcessingService сервис обработки заказов, который будет использоваться
     *                              для обработки сгенерированных заказов
     * @param meterRegistry регистр метрик для регистрации и обновления метрик
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
     * <p>
     * Выполняется каждые 5 секунд и генерирует случайное количество заказов
     * (от 1 до 3) с различными параметрами:
     * <ul>
     *   <li>Уникальный ID заказа (UUID)</li>
     *   <li>Случайная сумма заказа (от 100 до 1000)</li>
     *   <li>Случайный регион из предопределенного списка</li>
     * </ul>
     * </p>
     * <p>
     * При каждом запуске увеличивает счетчик выполнения задач, что позволяет
     * отслеживать работоспособность планировщика.
     * </p>
     * 
     * @see OrderProcessingService#processOrder(String, double, String)
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
     * <p>
     * Имитирует длительную задачу обслуживания системы, такую как:
     * <ul>
     *   <li>Очистка временных данных</li>
     *   <li>Индексация или архивирование</li>
     *   <li>Выполнение задач консолидации</li>
     * </ul>
     * </p>
     * <p>
     * Демонстрирует использование Timer для измерения времени выполнения длительных задач.
     * Время выполнения имитируется случайной задержкой от 2 до 5 секунд. 
     * Для этого класса Timer настроен для сбора и публикации процентилей (50-й и 95-й),
     * что позволяет анализировать распределение времени выполнения.
     * </p>
     * <p>
     * Используется лямбда-выражение в методе {@link Timer#record(Runnable)} для
     * автоматического измерения времени выполнения задачи.
     * </p>
     * 
     * @see Timer#record(Runnable)
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
                // Восстанавливаем флаг прерывания
                Thread.currentThread().interrupt();
                log.warn("Maintenance task interrupted");
            }
        });
    }
}