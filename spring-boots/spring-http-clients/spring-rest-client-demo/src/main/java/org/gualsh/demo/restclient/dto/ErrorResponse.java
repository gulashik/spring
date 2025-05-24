package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO для демонстрации обработки ошибок API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse implements Serializable {

    /**
     * Код ошибки.
     */
    private String code;

    /**
     * Сообщение об ошибке.
     */
    private String message;

    /**
     * Детали ошибки.
     */
    private String details;

    /**
     * Время возникновения ошибки.
     */
    private LocalDateTime timestamp;

    /**
     * Путь запроса, где произошла ошибка.
     */
    private String path;

    /**
     * HTTP статус код.
     */
    private Integer status;
}
