package org.gualsh.demo.httpe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * Модель для создания нового поста.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Отдельная модель для создания объектов
 * (без ID, который генерируется сервером).
 * </p>
 */
@Data
@Builder
public class CreatePostRequest {
    @NotNull
    @Positive
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String body;
}

