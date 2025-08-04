package org.gualsh.demo.curbreaker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Модель запроса для отправки email через Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данная модель демонстрирует правильное проектирование request DTO для сервисов,
 * которые используют Circuit Breaker. Важно отделять валидацию входных данных
 * от технических ошибок сервиса.
 * </p>
 *
 * <p><strong>Разделение типов ошибок:</strong></p>
 * <ul>
 *   <li>Validation errors - не влияют на Circuit Breaker</li>
 *   <li>Business logic errors - не влияют на Circuit Breaker</li>
 *   <li>Technical errors (network, timeout) - влияют на Circuit Breaker</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * EmailRequest request = EmailRequest.builder()
 *     .to("user@example.com")
 *     .subject("Test Subject")
 *     .body("Test message body")
 *     .from("sender@example.com")
 *     .build();
 * }</pre>
 *
 * @author Educational Demo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    /**
     * Email получателя.
     */
    @NotBlank(message = "Email получателя обязателен")
    @Email(message = "Некорректный формат email получателя")
    private String to;

    /**
     * Email отправителя.
     */
    @NotBlank(message = "Email отправителя обязателен")
    @Email(message = "Некорректный формат email отправителя")
    private String from;

    /**
     * Тема письма.
     */
    @NotBlank(message = "Тема письма обязательна")
    @Size(max = 200, message = "Тема письма не может превышать 200 символов")
    private String subject;

    /**
     * Тело письма.
     */
    @NotBlank(message = "Тело письма обязательно")
    @Size(max = 5000, message = "Тело письма не может превышать 5000 символов")
    private String body;

    /**
     * Приоритет письма (опционально).
     */
    private String priority;
}