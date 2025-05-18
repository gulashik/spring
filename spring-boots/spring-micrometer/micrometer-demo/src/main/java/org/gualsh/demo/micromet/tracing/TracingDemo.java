package org.gualsh.demo.micromet.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Компонент для демонстрации возможностей Micrometer Observation API для трассировки.
 * <p>
 * Micrometer Observation API позволяет создавать наблюдения (observations) для
 * трассировки операций и сбора метрик. Это унифицированный API для обоих видов
 * телеметрии: метрик и трассировки.
 * </p>
 */
@Component
@Slf4j
public class TracingDemo {

    private final ObservationRegistry observationRegistry;
    private final Random random = new Random();

    /**
     * Создает новый экземпляр демонстрации трассировки.
     *
     * @param observationRegistry реестр наблюдений Micrometer
     */
    public TracingDemo(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        log.info("Tracing demo initialized");

        // Запуск демонстрационного метода для создания некоторого начального трейса
        demoTrace();
    }

    /**
     * Демонстрирует использование Observation API для создания трейсов.
     *
     * @return результат операции
     */
    public String demoTrace() {
        // Создаем наблюдение для всей операции
        return Observation.createNotStarted("demo.operation", observationRegistry)
            .lowCardinalityKeyValue("operation.type", "demonstration")
            .highCardinalityKeyValue("demo.id", String.valueOf(System.currentTimeMillis()))
            .observe(() -> {
                log.info("Starting demo operation with tracing");

                try {
                    // Имитируем некоторую работу
                    TimeUnit.MILLISECONDS.sleep(100 + random.nextInt(200));

                    // Вложенная операция с отдельным наблюдением
                    String subResult = performSubOperation();

                    // Еще одна вложенная операция
                    int count = countItems();

                    return "Operation completed: " + subResult + ", items: " + count;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Operation interrupted";
                }
            });
    }

    /**
     * Демонстрирует вложенное наблюдение для подоперации.
     *
     * @return результат подоперации
     * @throws InterruptedException если операция была прервана
     */
    private String performSubOperation() throws InterruptedException {
        // Создаем вложенное наблюдение
        return Observation.createNotStarted("demo.sub_operation", observationRegistry)
            .lowCardinalityKeyValue("sub_operation.type", "processing")
            .observe(() -> {
                log.info("Performing sub-operation");

                try {
                    // Имитируем работу подоперации
                    TimeUnit.MILLISECONDS.sleep(50 + random.nextInt(100));

                    // Симулируем случайные ошибки
                    if (random.nextInt(10) == 0) {
                        throw new RuntimeException("Demonstration error in sub-operation");
                    }

                    return "Success";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Interrupted";
                }
            });
    }

    /**
     * Демонстрирует еще одну вложенную операцию с наблюдением.
     *
     * @return количество обработанных элементов
     * @throws InterruptedException если операция была прервана
     */
    private int countItems() throws InterruptedException {
        // Еще одно вложенное наблюдение
        return Observation.createNotStarted("demo.count_items", observationRegistry)
            .lowCardinalityKeyValue("count.type", "inventory")
            .observe(() -> {
                log.info("Counting items");

                try {
                    // Имитируем подсчет элементов
                    TimeUnit.MILLISECONDS.sleep(30 + random.nextInt(50));

                    // Генерируем случайное количество
                    return 10 + random.nextInt(90);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0;
                }
            });
    }

    /**
     * Выполняет трассируемую операцию с заданным ID.
     *
     * @param operationId ID операции для трассировки
     * @return результат операции
     */
    public String performTracedOperation(String operationId) {
        return Observation.createNotStarted("demo.custom_operation", observationRegistry)
            .lowCardinalityKeyValue("operation.type", "custom")
            .highCardinalityKeyValue("operation.id", operationId)
            .observe(() -> {
                log.info("Performing traced operation: {}", operationId);

                try {
                    // Имитируем выполнение операции
                    TimeUnit.MILLISECONDS.sleep(200 + random.nextInt(300));

                    return "Operation " + operationId + " completed successfully";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Operation " + operationId + " interrupted";
                }
            });
    }
}