package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для демонстрации работы с ParameterizedTypeReference.
 * Используется для десериализации коллекций и generic типов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> implements Serializable {

    /**
     * Статус ответа.
     */
    private String status;

    /**
     * Сообщение ответа.
     */
    private String message;

    /**
     * Данные ответа (generic тип).
     */
    private T data;

    /**
     * Метаданные ответа.
     */
    private Map<String, Object> metadata;

    /**
     * Время создания ответа.
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}