package org.gualsh.demo.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO для обновления поста.
 *
 * @author Demo
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostDto implements Serializable {

    /**
     * Идентификатор поста для обновления.
     */
    @NotNull
    private Long id;

    /**
     * Идентификатор пользователя.
     */
    @NotNull
    @JsonProperty("userId")
    private Long userId;

    /**
     * Новый заголовок поста.
     */
    @Size(max = 200)
    private String title;

    /**
     * Новое содержимое поста.
     */
    @Size(max = 2000)
    private String body;
}
