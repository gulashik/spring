package org.gualsh.demo.webclient.dto;

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

/**
 * DTO для поста из JSONPlaceholder API.
 *
 * <p>Представляет структуру блогового поста с заголовком и содержимым.</p>
 *
 * @author Demo
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostDto implements Serializable {

    /**
     * Уникальный идентификатор поста.
     */
    @NotNull
    private Long id;

    /**
     * Идентификатор пользователя, создавшего пост.
     */
    @NotNull
    @JsonProperty("userId")
    private Long userId;

    /**
     * Заголовок поста.
     */
    @NotBlank
    @Size(max = 200)
    private String title;

    /**
     * Содержимое поста.
     */
    @NotBlank
    @Size(max = 2000)
    private String body;
}
