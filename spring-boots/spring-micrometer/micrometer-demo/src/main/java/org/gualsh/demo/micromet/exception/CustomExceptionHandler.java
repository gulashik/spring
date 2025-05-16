package org.gualsh.demo.micromet.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений с интеграцией метрик.
 * Демонстрирует фиксацию метрик при возникновении ошибок.
 */
@ControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);
    private final MeterRegistry meterRegistry;

    public CustomExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Обрабатывает общие исключения RuntimeException.
     *
     * @param ex Возникшее исключение
     * @return Стандартизированный ответ об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception occurred", ex);

        // Инкрементируем метрику ошибок
        Counter.builder("exceptions.runtime")
            .description("Runtime exceptions count")
            .tag("exception", ex.getClass().getSimpleName())
            .tag("message", truncateMessage(ex.getMessage()))
            .register(meterRegistry)
            .increment();

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обрабатывает исключения IllegalArgumentException.
     *
     * @param ex Возникшее исключение
     * @return Стандартизированный ответ об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument provided", ex);

        // Инкрементируем метрику ошибок валидации
        Counter.builder("exceptions.validation")
            .description("Validation exceptions count")
            .tag("exception", "IllegalArgument")
            .tag("message", truncateMessage(ex.getMessage()))
            .register(meterRegistry)
            .increment();

        Map<String, Object> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Усекает длинные сообщения об ошибках, чтобы избежать создания
     * большого количества уникальных значений тегов.
     *
     * @param message Исходное сообщение
     * @return Усеченное сообщение
     */
    private String truncateMessage(String message) {
        if (message == null) {
            return "null";
        }

        // Ограничиваем длину сообщения до 50 символов
        if (message.length() > 50) {
            return message.substring(0, 47) + "...";
        }

        return message;
    }
}