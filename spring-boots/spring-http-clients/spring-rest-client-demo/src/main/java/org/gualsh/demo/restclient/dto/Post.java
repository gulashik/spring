package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO для представления поста из JSONPlaceholder API.
 *
 * Демонстрирует работу с простыми объектами и валидацию контента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post implements Serializable {

    @NotNull(message = "ID поста не может быть null")
    private Long id;

    @NotNull(message = "User ID не может быть null")
    @JsonProperty("userId")
    private Long userId;

    @NotBlank(message = "Заголовок поста не может быть пустым")
    @Size(max = 200, message = "Заголовок не может превышать 200 символов")
    private String title;

    @NotBlank(message = "Тело поста не может быть пустым")
    @Size(max = 2000, message = "Тело поста не может превышать 2000 символов")
    private String body;

    /**
     * Дополнительные теги для поста (демонстрация работы с коллекциями).
     */
    private List<String> tags;
}
