package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * Универсальный DTO для ответов от HTTPBin API.
 * HTTPBin возвращает различную информацию о HTTP запросах.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpBinResponse implements Serializable {

    /**
     * URL запроса.
     */
    private String url;

    /**
     * Заголовки запроса.
     */
    private Map<String, String> headers;

    /**
     * Параметры запроса.
     */
    private Map<String, Object> args;

    /**
     * Данные формы (для POST/PUT запросов).
     */
    private Map<String, Object> form;

    /**
     * JSON данные запроса.
     */
    private Object json;

    /**
     * Исходный IP адрес клиента.
     */
    private String origin;

    /**
     * Данные о загруженных файлах.
     */
    private Map<String, String> files;
}
