package org.gualsh.demo.micromet.service;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис, демонстрирующий использование метрик Micrometer в бизнес-логике.
 * <p>
 * Этот класс показывает, как интегрировать метрики в существующий код сервиса
 * для мониторинга его работы. Имитирует обработку заказов с различными метриками.
 * </p>
 */
@Service
@Slf4j
public class OrderProcessingService {

    private final MeterRegistry meterRegistry;
    private final Random random = new Random();
    private final AtomicInteger orderQueueSize = new AtomicInteger(0);
    private final Map<String, Double> orderPriceByRegion = new HashMap<>();
    private final Counter totalOrdersCounter;
    private final Counter failedOrdersCounter;
    private final Timer processingTimer;
    private final Timer paymentProcessingTimer;
    private final DistributionSummary orderSizeSummary;

    /**
     * Создает новый экземпляр сервиса обработки заказов с настроенными метриками.
     *
     * @param meterRegistry регистр метрик для регистрации и обновления метрик
     */
    public OrderProcessingService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Инициализируем метрики с разными тегами и конфигурациями
        totalOrdersCounter = Counter.builder("business.orders.total")
            .description("Total number of received orders")
            .register(meterRegistry);

        failedOrdersCounter = Counter.builder("business.orders.failed")
            .description("Number of failed orders")
            .register(meterRegistry);

        processingTimer = Timer.builder("business.orders.processing.time")
            .description("Order processing time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        paymentProcessingTimer = Timer.builder("business.payment.processing.time")
            .description("Payment processing time")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        orderSizeSummary = DistributionSummary
            .builder("business.orders.amount")
            .description("Order amount distribution")
            .baseUnit("USD")
            .publishPercentiles(0.5, 0.75, 0.9, 0.99)
            .register(meterRegistry);

        // Регистрируем gauge для отслеживания размера очереди заказов
        Gauge.builder("business.orders.queue.size", orderQueueSize, AtomicInteger::get)
            .description("Current orders queue size")
            .register(meterRegistry);

        // Инициализируем начальные данные для регионов
        Arrays.asList("us", "eu", "asia", "africa").forEach(region ->
            orderPriceByRegion.put(region, 0.0));

        // Многомерные метрики - средняя стоимость заказа по регионам
        orderPriceByRegion.forEach((region, initialValue) ->
            Gauge.builder("business.orders.avg.price", orderPriceByRegion, map -> map.get(region))
                .tag("region", region)
                .description("Average order price by region")
                .baseUnit("USD")
                .register(meterRegistry));

        log.info("OrderProcessingService initialized with metrics");
    }

    /**
     * Обрабатывает новый заказ и обновляет соответствующие метрики.
     *
     * @param orderId идентификатор заказа
     * @param amount сумма заказа
     * @param region регион заказа
     * @return успешность обработки заказа
     */
    public boolean processOrder(String orderId, double amount, String region) {
        log.info("Processing order: {}, amount: {}, region: {}", orderId, amount, region);

        // Увеличиваем счетчик всех заказов
        totalOrdersCounter.increment();

        // Увеличиваем размер очереди при получении заказа
        orderQueueSize.incrementAndGet();

        // Измеряем время обработки с помощью Timer
        return processingTimer.record(() -> {
            try {
                // Эмулируем обработку заказа
                simulateOrderProcessing();

                // Записываем метрику размера заказа
                orderSizeSummary.record(amount);

                // Обновляем среднюю сумму заказа для региона
                updateRegionOrderPrice(region, amount);

                // Обрабатываем платеж с отдельным таймером
                processPayment(amount);

                // Уменьшаем размер очереди после обработки
                orderQueueSize.decrementAndGet();

                // Симулируем случайные ошибки (примерно 10%)
                if (random.nextInt(10) == 0) {
                    failedOrdersCounter.increment();
                    log.warn("Order {} processing failed", orderId);
                    return false;
                }

                return true;
            } catch (Exception e) {
                log.error("Error processing order: " + orderId, e);
                failedOrdersCounter.increment();
                orderQueueSize.decrementAndGet();
                return false;
            }
        });
    }

    /**
     * Эмулирует обработку платежа и измеряет время выполнения.
     *
     * @param amount сумма платежа
     */
    private void processPayment(double amount) {
        paymentProcessingTimer.record(() -> {
            try {
                // Эмулируем время обработки платежа, зависящее от суммы
                TimeUnit.MILLISECONDS.sleep(50 + (long)(amount * 0.1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Обновляет среднюю цену заказа для указанного региона.
     *
     * @param region регион для обновления статистики
     * @param newOrderAmount сумма нового заказа
     */
    private void updateRegionOrderPrice(String region, double newOrderAmount) {
        // Если регион не задан, используем значение по умолчанию
        String regionKey = region != null ? region : "unknown";

        // Получаем текущее значение для региона
        Double currentValue = orderPriceByRegion.getOrDefault(regionKey, 0.0);

        // Простой способ обновления "скользящего среднего" - смешивание
        double newAverage = currentValue * 0.8 + newOrderAmount * 0.2;

        // Обновляем значение в Map, это автоматически обновит Gauge
        orderPriceByRegion.put(regionKey, newAverage);
    }

    /**
     * Эмулирует обработку заказа с разным временем выполнения.
     */
    private void simulateOrderProcessing() {
        try {
            // Случайное время обработки от 100мс до 500мс
            TimeUnit.MILLISECONDS.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Возвращает текущий размер очереди заказов.
     *
     * @return текущее количество заказов в очереди
     */
    public int getQueueSize() {
        return orderQueueSize.get();
    }
}