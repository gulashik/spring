package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO для представления комментария к посту.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comment implements Serializable {

    private Long id;

    @JsonProperty("postId")
    private Long postId;

    @NotBlank(message = "Имя автора комментария не может быть пустым")
    private String name;

    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Тело комментария не может быть пустым")
    private String body;
}
