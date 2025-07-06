package org.gualsh.demo.httpe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * Модель комментария из JSONPlaceholder API.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Комбинация Lombok и validation аннотаций
 * для создания robust DTO.
 * </p>
 */
@Data
@Builder
public class Comment {
    @NotNull
    @Positive
    private Long id;

    @NotNull
    @Positive
    private Long postId;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String body;
}

