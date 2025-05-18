package org.gualsh.demo.micromet.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.micromet.service.OrderProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * REST контроллер демонстрирующий различные способы измерения производительности
 * с использованием библиотеки Micrometer и таймеров.
 * <p>
 * Этот контроллер предоставляет API для мониторинга состояния системы, управления
 * ресурсами, обработки заказов и тестирования производительности. Он демонстрирует
 * как декларативный подход с использованием аннотации {@link Timed}, так и
 * программный с прямым использованием классов {@link Timer} и {@link MeterRegistry}.
 * </p>
 * <p>
 * Основные особенности:
 * <ul>
 *   <li>Автоматическое измерение времени выполнения методов с помощью аннотации @Timed</li>
 *   <li>Ручное измерение времени с использованием API Timer</li>
 *   <li>Интеграция с бизнес-метриками через OrderProcessingService</li>
 *   <li>Генерация контролируемых ошибок для тестирования устойчивости системы мониторинга</li>
 *   <li>Создание искусственной нагрузки для тестирования производительности</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class TimedController {

    /**
     * Основной регистр метрик Micrometer.
     * Используется для создания и регистрации таймеров и других метрик.
     */
    private final MeterRegistry meterRegistry;
    
    /**
     * Сервис для обработки заказов и обновления бизнес-метрик.
     * Предоставляет методы для учета обработанных заказов и получения размера очереди.
     */
    private final OrderProcessingService orderProcessingService;

    /**
     * Генератор случайных чисел для имитации различного поведения и задержек.
     * Используется для создания реалистичных сценариев нагрузки и симуляции
     * вариабельности времени обработки запросов.
     */
    private final Random random = new Random();
    
    /**
     * Массив регионов для использования в тестовых данных.
     * Используется при генерации тестовых заказов для имитации географического распределения.
     */
    private final String[] regions = {"us", "eu", "asia", "africa"};
    
    /**
     * Предварительно созданный таймер для метода getResources().
     * Измеряет время выполнения метода и предоставляет метрики о его производительности.
     */
    private final Timer resourcesTimer;

    /**
     * Создает новый экземпляр контроллера с таймерами и метриками.
     * <p>
     * В конструкторе происходит инициализация и регистрация таймера для метода
     * {@link #getResources()}, демонстрируя программный подход к созданию метрик.
     * </p>
     *
     * @param meterRegistry регистр метрик для регистрации таймеров и других метрик
     * @param orderProcessingService сервис для обработки заказов и получения бизнес-метрик
     */
    public TimedController(MeterRegistry meterRegistry, OrderProcessingService orderProcessingService) {
        this.meterRegistry = meterRegistry;
        
        // Регистрируем Timer один раз при создании контроллера
        // Создается с помощью билдера и регистрируется в MeterRegistry
        this.resourcesTimer = Timer.builder("api.resources.request")
            .description("Time taken to return resources info")
            .tag("method", "GET")
            .register(meterRegistry);

        this.orderProcessingService = orderProcessingService;
    }

    /**
     * Возвращает текущий статус сервера и размер очереди заказов.
     * <p>
     * Метод демонстрирует использование аннотации {@link Timed} для автоматического
     * измерения времени выполнения. Аннотация настроена для сбора различных
     * процентилей (50-й, 90-й, 95-й, 99-й), что позволяет более детально
     * анализировать производительность метода.
     * </p>
     * <p>
     * Требует регистрации бина {@link io.micrometer.core.aop.TimedAspect},
     * который определен в классе {@code MicrometerConfig}.
     * </p>
     *
     * @return {@link ResponseEntity} с информацией о статусе сервера, временной метке
     *         и текущем размере очереди заказов
     */
    @GetMapping("/status")
    @Timed(
        value = "api.status.request",
        description = "Time taken to return status",
        percentiles = {0.5, 0.9, 0.95, 0.99} // Конфигурируется с параметрами для сбора процентилей
    )
    public ResponseEntity<Map<String, Object>> getStatus() {
        // Имитируем небольшую задержку для демонстрации метрик
        randomDelay(50);

        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("queue_size", orderProcessingService.getQueueSize());

        return ResponseEntity.ok(status);
    }

    /**
     * Возвращает информацию о системных ресурсах сервера.
     * <p>
     * Этот метод демонстрирует ручное измерение времени выполнения с использованием
     * предварительно созданного таймера {@link #resourcesTimer} и класса {@link Timer.Sample}.
     * Такой подход дает больше контроля над процессом измерения, позволяя
     * измерять только определенные участки кода или условные ветки выполнения.
     * </p>
     * <p>
     * Информация о ресурсах включает в себя симулированное использование CPU
     * и актуальную информацию о памяти JVM.
     * </p>
     *
     * @return {@link ResponseEntity} с информацией о системных ресурсах:
     *         использование CPU (симулированное), используемая память,
     *         свободная память и общий объем памяти JVM
     */
    @GetMapping("/resources")
    public ResponseEntity<Map<String, Object>> getResources() {
        // todo Используем Timer напрямую вместо аннотации
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // Имитируем обработку
            randomDelay(100);

            Map<String, Object> resources = new HashMap<>();
            resources.put("cpu_usage", random.nextInt(100));
            resources.put("memory_used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            resources.put("memory_free", Runtime.getRuntime().freeMemory());
            resources.put("total_memory", Runtime.getRuntime().totalMemory());

            return ResponseEntity.ok(resources);
        } finally {
            // todo Обязательно останавливаем таймер и записываем результат
            sample.stop(resourcesTimer);
        }
    }

    /**
     * Обрабатывает заказ и обновляет бизнес-метрики.
     * <p>
     * Метод демонстрирует интеграцию системы метрик с бизнес-логикой приложения,
     * передавая данные заказа в сервис {@link OrderProcessingService}. Таким образом,
     * можно отслеживать бизнес-показатели такие как количество заказов, распределение
     * по регионам и сумме заказа.
     * </p>
     * <p>
     * Также метод демонстрирует использование аннотации {@link Timed} без
     * дополнительных параметров, что обеспечивает базовое измерение времени выполнения.
     * </p>
     *
     * @param orderRequest данные заказа в JSON формате, включая опциональные поля
     *                    'amount' (сумма заказа) и 'region' (регион)
     * @return {@link ResponseEntity} с результатом обработки заказа, включая
     *         сгенерированный ID заказа, статус обработки, сумму и регион
     */
    @PostMapping("/orders")
    @Timed("api.orders.creation")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        String orderId = UUID.randomUUID().toString();

        // Извлекаем данные из запроса или используем значения по умолчанию
        double amount = orderRequest.containsKey("amount")
            ? Double.parseDouble(orderRequest.get("amount").toString())
            : 100.0 + random.nextDouble() * 900.0;

        String region = orderRequest.containsKey("region")
            ? orderRequest.get("region").toString()
            : regions[random.nextInt(regions.length)];

        // Обрабатываем заказ через сервис, который обновляет метрики
        boolean success = orderProcessingService.processOrder(orderId, amount, region);

        Map<String, Object> response = new HashMap<>();
        response.put("order_id", orderId);
        response.put("status", success ? "success" : "failed");
        response.put("amount", amount);
        response.put("region", region);

        return ResponseEntity.ok(response);
    }

    /**
     * Генерирует различные типы исключений для тестирования системы мониторинга ошибок.
     * <p>
     * Этот метод позволяет тестировать, как система мониторинга реагирует на
     * различные типы ошибок и исключений. Он может генерировать три типа исключений:
     * <ul>
     *   <li>RuntimeException - для общих ошибок времени выполнения</li>
     *   <li>IllegalArgumentException - для ошибок, связанных с аргументами</li>
     *   <li>NullPointerException - для симуляции разыменования null-ссылки</li>
     * </ul>
     * </p>
     * <p>
     * Важно: этот метод всегда завершается исключением и никогда не возвращает результат.
     * </p>
     *
     * @param type строковый идентификатор типа генерируемой ошибки: "runtime", "illegal" или "nullpointer"
     * @return теоретически возвращает ResponseEntity со строкой, но на практике никогда
     *         не возвращает значение, так как всегда выбрасывает исключение
     * @throws RuntimeException при type="runtime"
     * @throws IllegalArgumentException при type="illegal"
     * @throws NullPointerException при type="nullpointer"
     * @throws UnsupportedOperationException при неизвестном типе ошибки
     */
    @GetMapping("/error/{type}")
    public ResponseEntity<String> generateError(@PathVariable String type) {
        log.info("Generating error of type: {}", type);

        switch (type) {
            case "runtime":
                throw new RuntimeException("Демонстрационная ошибка времени выполнения");
            case "illegal":
                throw new IllegalArgumentException("Демонстрационная ошибка неверного аргумента");
            case "nullpointer":
                // Симулируем NullPointerException
                String nullStr = null;
                return ResponseEntity.ok(nullStr.toLowerCase());
            default:
                throw new UnsupportedOperationException("Неизвестный тип ошибки: " + type);
        }
    }

    /**
     * Генерирует искусственную нагрузку на систему для тестирования производительности.
     * <p>
     * Метод создает множество запросов на обработку заказов в течение указанного
     * времени, что позволяет проверить, как система справляется с повышенной нагрузкой
     * и как работает система мониторинга в таких условиях.
     * </p>
     * <p>
     * Из соображений безопасности, максимальная длительность генерации нагрузки
     * ограничена 60 секундами.
     * </p>
     *
     * @param seconds длительность генерации нагрузки в секундах (по умолчанию 10 секунд)
     * @return {@link ResponseEntity} с информацией о проведенном тесте: количество
     *         обработанных запросов и фактическая длительность теста
     */
    @GetMapping("/load-test")
    public ResponseEntity<Map<String, Object>> generateLoad(@RequestParam(defaultValue = "10") int seconds) {
        log.info("Generating load for {} seconds", seconds);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (seconds * 1000);
        int requestCount = 0;

        // Ограничиваем максимальное время работы в 60 секунд для безопасности
        seconds = Math.min(seconds, 60);

        while (System.currentTimeMillis() < endTime) {
            // Симулируем создание заказа
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("amount", 100.0 + random.nextDouble() * 900.0);
            orderData.put("region", regions[random.nextInt(regions.length)]);

            createOrder(orderData);
            requestCount++;

            // Небольшая пауза между запросами
            randomDelay(20);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("load_test", "completed");
        result.put("requests_processed", requestCount);
        result.put("duration_seconds", seconds);

        return ResponseEntity.ok(result);
    }

    /**
     * Добавляет случайную задержку для имитации вариабельного времени обработки запросов.
     * <p>
     * Этот вспомогательный метод создает задержку, состоящую из базового времени
     * плюс случайная составляющая. Это позволяет имитировать более реалистичную
     * картину производительности, где время обработки запросов варьируется.
     * </p>
     * <p>
     * Фактическая задержка будет в диапазоне от baseMillis до baseMillis*2.
     * </p>
     *
     * @param baseMillis базовое время задержки в миллисекундах, к которому
     *                  добавляется случайная составляющая от 0 до baseMillis
     */
    private void randomDelay(int baseMillis) {
        try {
            // Базовая задержка плюс случайная составляющая
            TimeUnit.MILLISECONDS.sleep(baseMillis + random.nextInt(baseMillis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}