package org.gualsh.demo.httpe.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Модель для обновления поста.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Отдельная модель для обновления,
 * где все поля опциональны (partial update).
 * </p>
 */
@Data
@Builder
public class UpdatePostRequest {
    private Long userId;
    private String title;
    private String body;
}

