package org.gualsh.demo.httpe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Модель поста из JSONPlaceholder API.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Record класс для неизменяемых данных.
 * Преимущества record:
 * </p>
 * <ul>
 * <li>Компактный синтаксис</li>
 * <li>Автоматическая неизменяемость</li>
 * <li>Автогенерация equals/hashCode/toString</li>
 * <li>Безопасность потоков</li>
 * </ul>
 *
 * <pre>{@code
 * Post post = new Post(1L, 1L, "Title", "Content");
 *
 * // Получение данных
 * Long id = post.id();
 * String title = post.title();
 * }</pre>
 */
public record Post(
    @NotNull @Positive Long id,
    @NotNull @Positive Long userId,
    @NotBlank String title,
    @NotBlank String body
) {
    /**
     * Создает новый пост с обновленным заголовком.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Поскольку record неизменяемый, для изменения данных
     * нужно создавать новый экземпляр.
     * </p>
     *
     * @param newTitle новый заголовок
     * @return новый пост с обновленным заголовком
     */
    public Post withTitle(String newTitle) {
        return new Post(id, userId, newTitle, body);
    }
}
