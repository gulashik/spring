package ru.gulash.actuatordemo.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Демонстрационный REST контроллер для генерации нагрузки
 * и демонстрации мониторинга через Actuator.
 */
@RestController
@RequestMapping("/api")
//@RequiredArgsConstructor
public class DemoController {

    // Инжектируем реестр метрик для создания и регистрации счетчиков
    private final MeterRegistry meterRegistry;

    // Счетчик будет учитывать количество запросов к /api/hello
    private final Counter helloCounter;

    // Создание счетчика через конструктор
    public DemoController(
        MeterRegistry meterRegistry
    ) {
        this.meterRegistry = meterRegistry;

        this.helloCounter = Counter.builder("api.hello.requests")
            .description("Счетчик запросов к /api/hello")
            .register(meterRegistry);
    }

    /**
     * Простой эндпоинт, увеличивающий счетчик при вызове
     */
    @GetMapping("/hello")
    public String hello() {
        helloCounter.increment();
        return "Hello from Actuator Demo!";
    }

    /**
     * Эндпоинт для создания искусственной задержки
     * чтобы увидеть это в метриках
     */
    @GetMapping("/delay/{seconds}")
    public String delayResponse(@PathVariable int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Response after " + seconds + " seconds delay";
    }

    /**
     * Эндпоинт для генерации исключения
     * для демонстрации метрик ошибок
     */
    @GetMapping("/error")
    public String generateError() {
        throw new RuntimeException("Demo exception for actuator metrics");
    }
}
