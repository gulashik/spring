package org.gualsh.demo.webclient.dto;

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
 * DTO для создания нового поста.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto implements Serializable {

    /**
     * Идентификатор пользователя, создающего пост.
     */
    @NotNull
    @JsonProperty("userId")
    private Long userId;

    /**
     * Заголовок нового поста.
     */
    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    /**
     * Содержимое нового поста.
     */
    @NotBlank
    @Size(min = 10, max = 2000)
    private String body;
}
