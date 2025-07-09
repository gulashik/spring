package org.gualsh.demo.webclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO для комментария из JSONPlaceholder API.
 *
 * @author Demo
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentDto implements Serializable {

    /**
     * Уникальный идентификатор комментария.
     */
    @NotNull
    private Long id;

    /**
     * Идентификатор поста, к которому относится комментарий.
     */
    @NotNull
    @JsonProperty("postId")
    private Long postId;

    /**
     * Имя автора комментария.
     */
    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * Email автора комментария.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Текст комментария.
     */
    @NotBlank
    @Size(max = 1000)
    private String body;
}