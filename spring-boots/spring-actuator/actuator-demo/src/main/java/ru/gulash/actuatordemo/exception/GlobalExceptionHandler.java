package ru.gulash.actuatordemo.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Глобальный обработчик исключений.
 * Перехватывает исключения и регистрирует их в метриках.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;
    private final Map<Class<? extends Exception>, Counter> exceptionCounters = new ConcurrentHashMap<>();

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Обработчик для RuntimeException и его подклассов.
     * Регистрирует исключение в метриках и возвращает стандартизированный ответ.
     *
     * @param ex перехваченное исключение
     * @return стандартизированный ответ с информацией об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // Логирование ошибки
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        // Увеличение счетчика для данного типа исключения
        getOrCreateCounter(ex.getClass()).increment();

        // Формирование стандартизированного ответа
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        response.put("exception", ex.getClass().getName());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Получает или создает счетчик для данного типа исключения.
     *
     * @param exceptionClass класс исключения
     * @return счетчик для данного типа исключения
     */
    private Counter getOrCreateCounter(Class<? extends Exception> exceptionClass) {
        return exceptionCounters.computeIfAbsent(exceptionClass, cls -> {
            String metricName = "app.exceptions";
            return Counter.builder(metricName)
                .tag("exception", cls.getSimpleName())
                .description("Счетчик исключений типа " + cls.getName())
                .register(meterRegistry);
        });
    }
}
