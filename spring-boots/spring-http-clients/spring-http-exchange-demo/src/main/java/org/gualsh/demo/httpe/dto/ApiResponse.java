package org.gualsh.demo.httpe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Общая модель ответа API.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Generic wrapper для унификации ответов API.
 * Содержит метаданные о результате операции.
 * </p>
 */
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    /**
     * Создает успешный ответ.
     *
     * @param data данные
     * @param <T> тип данных
     * @return успешный ответ
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Создает ответ с ошибкой.
     *
     * @param message сообщение об ошибке
     * @param <T> тип данных
     * @return ответ с ошибкой
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

