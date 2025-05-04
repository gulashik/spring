package ru.gulash.actuatordemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gulash.actuatordemo.service.MetricsService;

/**
 * Контроллер для демонстрации работы с метриками и таймерами.
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Демонстрация метода с аннотацией @Timed
     */
    @GetMapping("/timed/{input}")
    public String timedOperation(@PathVariable String input) {
        return metricsService.performTimedOperation(input);
    }

    /**
     * Демонстрация метода с ручным измерением через Timer
     */
    @GetMapping("/manual/{input}")
    public String manualTiming(@PathVariable String input) {
        return metricsService.recordExecutionTime(
            data -> "Processed with timer: " + data,
            input
        );
    }
}
