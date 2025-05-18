package org.gualsh.demo.micromet.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;

/**
 * Компонент для создания и демонстрации различных типов метрик Micrometer.
 * <p>
 * Этот класс реализует интерфейс MeterBinder, что позволяет зарегистрировать
 * все метрики сразу при инициализации бина. Демонстрируются основные типы
 * метрик: Counter, Gauge, Timer, DistributionSummary и многомерные метрики с тегами.
 * </p>
 */
@Component
@Slf4j
public class CoreMetricsDemo implements MeterBinder {

    // Поля для демонстрации измерений
    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final Random random = new Random();
    private List<String> regions = Arrays.asList("us-east", "us-west", "eu-central", "ap-south");

    /**
     * Регистрирует различные типы метрик в переданном MeterRegistry.
     * Этот метод вызывается автоматически при создании бина.
     *
     * @param registry регистр метрик для регистрации
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Registering core metrics demo");

        // 1. COUNTER - счетчик, который может только увеличиваться
        Counter requestCounter = Counter.builder("app.requests.total")
            .description("Total number of requests")
            .tags("type", "demo")
            .register(registry);

        // Добавим начальное значение для демонстрации
        requestCounter.increment(42);

        // 2. GAUGE - значение, которое может увеличиваться и уменьшаться
        Gauge queueSizeGauge = Gauge.builder("app.queue.size", queueSize, AtomicInteger::get)
            .description("Current queue size")
            .tags("queue", "default")
            .register(registry);

        // Задаем начальное значение
        queueSize.set(10);

        // 3. TIMER - для измерения длительности операций
        Timer sampleTimer = Timer.builder("app.request.latency")
            .description("Request latency")
            .tags("endpoint", "sample")
            .publishPercentileHistogram()
            .publishPercentiles(0.5, 0.95, 0.99)
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(registry);

        // Зарегистрируем тестовые измерения
        sampleTimer.record(Duration.ofMillis(100));
        sampleTimer.record(Duration.ofMillis(200));

        // 4. DISTRIBUTION SUMMARY - для произвольных числовых распределений (не времени)
        DistributionSummary requestSizeDistribution = DistributionSummary.builder("app.request.size")
            .description("Request size distribution in bytes")
            .baseUnit("bytes")
            .tags("protocol", "http")
            .publishPercentileHistogram()
            .minimumExpectedValue(100.0)
            .maximumExpectedValue(10000.0)
            .register(registry);

        // Зарегистрируем тестовые значения
        requestSizeDistribution.record(256);
        requestSizeDistribution.record(1024);
        requestSizeDistribution.record(4096);

        // 5. Многомерные метрики с различными тегами
        // Создаем счетчики для разных регионов
        regions.forEach(region -> {
            Counter regionCounter = Counter.builder("app.requests.by.region")
                .description("Requests count by region")
                .tags("region", region)
                .register(registry);

            // Добавляем случайные значения
            regionCounter.increment(random.nextInt(100));
        });

        // 6. Использование функционального интерфейса для Gauge
        // Это полезно для измерения значений в существующих объектах
        Gauge functionGauge = Gauge.builder("app.memory.free", this, value ->
                Runtime.getRuntime().freeMemory())
            .description("JVM free memory")
            .baseUnit("bytes")
            .register(registry);

        // 7. Создадим свой тип метрики - счетчик с автоматическим сбросом
        FunctionCounter processingRate = FunctionCounter.builder("app.processing.rate",
                this, obj -> random.nextDouble() * 100)
            .description("Processing rate")
            .baseUnit("ops")
            .register(registry);

        // 8. Демонстрация LongTaskTimer для длительных задач
        LongTaskTimer longRunningTasks = LongTaskTimer.builder("app.tasks.long.running")
            .description("Long running tasks")
            .register(registry);

        // Симулируем запуск длительной задачи
        Runnable task = () -> {
            LongTaskTimer.Sample sample = longRunningTasks.start();
            try {
                Thread.sleep(5000); // 5 секунд
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sample.stop();
            }
        };

        // Запустим задачу в отдельном потоке
        new Thread(task).start();

        log.info("Core metrics demo registered");
    }
}