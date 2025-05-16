package org.gualsh.demo.micromet.metric;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Компонент для демонстрации различных типов пользовательских метрик.
 */
@Component
public class CustomMetrics {

    private final AtomicInteger businessValue = new AtomicInteger(0);
    private final MeterRegistry meterRegistry;
    private final List<String> items = new ArrayList<>();

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Регистрация различных типов метрик для демонстрации
        registerCounters();
        registerGauges();
        registerTimers();
        registerDistributionSummaries();
        registerLongTaskTimers();
        registerFunctionCounters();
        registerFunctionTimers();
        registerTimeGauges();
    }

    /**
     * Регистрирует счетчики.
     * Счетчики - это кумулятивные метрики, которые представляют одно числовое значение, которое может только увеличиваться.
     * Используются для измерения количества событий, запросов, ошибок и т.д.
     */
    private void registerCounters() {
        // Простой счетчик
        Counter counter = Counter.builder("custom.counter")
            .description("Демонстрационный счетчик")
            .tag("purpose", "demo")
            .register(meterRegistry);
        counter.increment();

        // Счетчик с тегами
        Counter.builder("custom.counter.tagged")
            .description("Счетчик с тегами")
            .tag("region", "west")
            .tag("environment", "demo")
            .register(meterRegistry)
            .increment(2);
    }

    /**
     * Регистрирует индикаторы (gauges).
     * Gauges - это метрики, которые представляют одно числовое значение, которое может увеличиваться или уменьшаться.
     * Используются для измерения текущих значений, таких как размер коллекции, температура, использование памяти и т.д.
     */
    private void registerGauges() {
        // Простой gauge, привязанный к атомарной переменной
        Gauge.builder("custom.gauge", businessValue, AtomicInteger::get)
            .description("Демонстрационный gauge")
            .tag("purpose", "demo")
            .register(meterRegistry);

        // Увеличиваем значение для демонстрации
        businessValue.set(100);

        // Gauge, привязанный к размеру коллекции
        items.add("item1");
        items.add("item2");

        Gauge.builder("custom.gauge.collection", items, List::size)
            .description("Размер демонстрационной коллекции")
            .tag("collection", "items")
            .register(meterRegistry);

        // Gauge с пользовательской функцией
        Gauge.builder("custom.gauge.computed", this, CustomMetrics::getComputedValue)
            .description("Вычисляемое значение gauge")
            .tag("type", "computed")
            .register(meterRegistry);
    }

    /**
     * Вычисляемое значение для gauge
     * @return Демонстрационное вычисляемое значение
     */
    private double getComputedValue() {
        return businessValue.get() * 1.5;
    }

    /**
     * Регистрирует таймеры.
     * Таймеры измеряют как количество событий, так и их продолжительность.
     * Используются для измерения задержки выполнения кода, API, транзакций и т.д.
     */
    private void registerTimers() {
        // Простой таймер
        Timer timer = Timer.builder("custom.timer")
            .description("Демонстрационный таймер")
            .tag("purpose", "demo")
            .publishPercentiles(0.5, 0.95, 0.99) // Публикуем медиану и 95-й, 99-й перцентили
            .publishPercentileHistogram() // Создаем гистограмму для визуализации
            .minimumExpectedValue(Duration.ofMillis(1))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(meterRegistry);

        // Записываем несколько значений для демонстрации
        timer.record(100, TimeUnit.MILLISECONDS);
        timer.record(200, TimeUnit.MILLISECONDS);
        timer.record(300, TimeUnit.MILLISECONDS);

        // Таймер с использованием Sample API
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Thread.sleep(50); // Имитируем работу
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        sample.stop(meterRegistry.timer("custom.timer.sampled", "method", "sample"));
    }

    /**
     * Регистрирует DistributionSummary (суммирование распределения).
     * Используется для отслеживания распределения размеров, весов, длительностей и других
     * числовых значений, которые не являются временем.
     */
    private void registerDistributionSummaries() {
        // Простое DistributionSummary
        DistributionSummary summary = DistributionSummary.builder("custom.summary")
            .description("Демонстрационное распределение")
            .tag("purpose", "demo")
            .baseUnit("bytes") // Единицы измерения
            .scale(1.0) // Множитель масштабирования
            .publishPercentiles(0.5, 0.75, 0.95)
            .minimumExpectedValue(1.0)
            .maximumExpectedValue(10000.0)
            .register(meterRegistry);

        // Записываем несколько значений
        summary.record(512);
        summary.record(1024);
        summary.record(2048);
        summary.record(4096);
    }

    /**
     * Регистрирует LongTaskTimer (таймер длительных задач).
     * Используется для измерения продолжительности длительных задач, показывая
     * текущую длительность и количество активных задач.
     */
    private void registerLongTaskTimers() {
        LongTaskTimer longTaskTimer = LongTaskTimer.builder("custom.long.task")
            .description("Демонстрационный таймер длительных задач")
            .tag("purpose", "demo")
            .register(meterRegistry);

        // Для демонстрации, запускаем и останавливаем длительную задачу
        LongTaskTimer.Sample longTaskSample = longTaskTimer.start();
        try {
            Thread.sleep(100); // Имитируем длительную работу
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        longTaskSample.stop();
    }

    /**
     * Регистрирует FunctionCounter (функциональный счетчик).
     * Используется для отслеживания счетчика из внешнего источника через функцию.
     */
    private void registerFunctionCounters() {
        // Создаем функциональный счетчик на основе внешнего значения
        FunctionCounter.builder("custom.function.counter", this, obj -> businessValue.get() * 10)
            .description("Демонстрационный функциональный счетчик")
            .tag("purpose", "demo")
            .register(meterRegistry);
    }

    /**
     * Регистрирует FunctionTimer (функциональный таймер).
     * Используется для измерения времени выполнения из внешнего источника через функции.
     */
    private void registerFunctionTimers() {
        // Создаем функциональный таймер, который получает данные через функции
        FunctionTimer.builder("custom.function.timer",
                this,
                obj -> 10, // кол-во вызовов
                obj -> 2.5, // суммарное время в секундах
                TimeUnit.SECONDS)
            .description("Демонстрационный функциональный таймер")
            .tag("purpose", "demo")
            .register(meterRegistry);
    }

    /**
     * Регистрирует TimeGauge (временной индикатор).
     * Используется для измерения времени, которое может изменяться.
     */
    private void registerTimeGauges() {
        // Создаем TimeGauge
        TimeGauge.builder("custom.time.gauge", this, TimeUnit.MILLISECONDS, obj -> 500.0)
            .description("Демонстрационный временной gauge")
            .tag("purpose", "demo")
            .register(meterRegistry);
    }
}