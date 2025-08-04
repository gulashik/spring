package org.gualsh.demo.curbreaker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Модель пользователя для демонстрации работы с базой данных через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данная модель демонстрирует использование Bean Validation API для валидации данных.
 * Валидация важна при работе с Circuit Breaker, так как неправильные данные
 * не должны считаться техническими ошибками и вызывать срабатывание Circuit Breaker.
 * </p>
 *
 * <p><strong>Принципы валидации в контексте Circuit Breaker:</strong></p>
 * <ul>
 *   <li>Бизнес-ошибки (validation failures) не должны влиять на Circuit Breaker</li>
 *   <li>Только технические ошибки (network, timeout) должны учитываться</li>
 *   <li>Используйте @Valid в контроллерах для ранней валидации</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * User user = User.builder()
 *     .id(1L)
 *     .name("John Doe")
 *     .email("john@example.com")
 *     .build();
 * }</pre>
 *
 * @author Educational Demo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Имя пользователя.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * @NotBlank проверяет, что поле не null, не пустое и содержит не только пробелы.
     * @Size ограничивает длину строки, что помогает предотвратить потенциальные
     * проблемы с базой данных.
     * </p>
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String name;

    /**
     * Email пользователя с валидацией формата.
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    /**
     * Дополнительная информация о пользователе.
     */
    @Size(max = 500, message = "Дополнительная информация не может превышать 500 символов")
    private String additionalInfo;
}
