package ru.gulash.actuatordemo.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Сервис для демонстрации различных типов метрик Micrometer.
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final Timer processTimer;
    private final Counter processCounter;
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final Random random = new Random();

    /**
     * Конструктор с инициализацией метрик
     *
     * @param meterRegistry реестр метрик
     */
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Создаем таймер для измерения времени выполнения операций
        this.processTimer = Timer.builder("app.processing.time")
            .description("Время выполнения операций обработки")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(meterRegistry);

        // Создаем счетчик для подсчета вызовов
        this.processCounter = Counter.builder("app.processing.count")
            .description("Количество операций обработки")
            .tag("type", "demo")
            .register(meterRegistry);

        // Создаем gauge для наблюдения за размером очереди
        Gauge.builder("app.queue.size", queueSize, AtomicInteger::get)
            .description("Текущий размер очереди задач")
            .register(meterRegistry);

        // Запускаем имитацию изменения размера очереди
        simulateQueueSizeChanges();
    }

    /**
     * Метод с аннотацией @Timed для автоматического измерения времени выполнения
     * с помощью аспектов Spring
     */
    @Timed("app.timed.operation")
    public String performTimedOperation(String input) {
        try {
            // Случайная задержка для имитации работы
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed: " + input;
    }

    /**
     * Метод с ручным измерением времени выполнения
     */
    public <T, R> R recordExecutionTime(Function<T, R> operation, T input) {
        // Запись времени выполнения с использованием таймера
        return processTimer.record(() -> {
            processCounter.increment();
            try {
                // Имитация обработки
                Thread.sleep(random.nextInt(500));
                return operation.apply(input);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Operation interrupted", e);
            }
        });
    }

    /**
     * Имитация изменения размера очереди для демонстрации gauge метрики
     */
    private void simulateQueueSizeChanges() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Случайно меняем размер очереди
                    int newSize = random.nextInt(20);
                    queueSize.set(newSize);
                    log.debug("Queue size changed to: {}", newSize);
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
