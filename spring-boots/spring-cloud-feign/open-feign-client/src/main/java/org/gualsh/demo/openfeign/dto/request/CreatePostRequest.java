package org.gualsh.demo.openfeign.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса создания нового поста через JSONPlaceholder API.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Request DTO отличаются от Response DTO несколькими важными аспектами:
 * </p>
 *
 * <h3>Ключевые отличия Request DTO:</h3>
 * <ul>
 *   <li><strong>Более строгая валидация</strong> - данные должны быть корректными перед отправкой</li>
 *   <li><strong>Только необходимые поля</strong> - ID часто не включается (генерируется сервером)</li>
 *   <li><strong>Бизнес-валидация</strong> - проверка бизнес-правил перед отправкой</li>
 *   <li><strong>Документация обязательности</strong> - четкое указание required полей</li>
 * </ul>
 *
 * <h3>Best Practices для Request DTO:</h3>
 * <ul>
 *   <li>Используйте отдельные DTO для запросов и ответов</li>
 *   <li>Применяйте строгую валидацию на уровне DTO</li>
 *   <li>Документируйте ограничения внешнего API</li>
 *   <li>Предусматривайте возможность расширения без breaking changes</li>
 * </ul>
 *
 * <h3>Подводные камни:</h3>
 * <ul>
 *   <li>Не включайте поля, которые должен генерировать сервер (ID, timestamps)</li>
 *   <li>Учитывайте ограничения длины полей в external API</li>
 *   <li>Проверяйте совместимость кодировки для различных языков</li>
 *   <li>Всегда валидируйте данные перед отправкой</li>
 * </ul>
 *
 * @author Generated for educational purposes
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    /**
     * Идентификатор пользователя, создающего пост.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * В реальных приложениях userId часто извлекается из контекста безопасности
     * (SecurityContext), а не передается в запросе. Здесь мы включаем его
     * для демонстрации работы с JSONPlaceholder API.
     * </p>
     */
    @JsonProperty("userId")
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    /**
     * Заголовок создаваемого поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @Size аннотация ограничивает длину заголовка. Это важно для:
     * </p>
     * <ul>
     *   <li>Предотвращения ошибок на стороне внешнего API</li>
     *   <li>Улучшения UX (предупреждение пользователя о лимитах)</li>
     *   <li>Защиты от потенциальных DoS атак через большие данные</li>
     * </ul>
     */
    @JsonProperty("title")
    @NotBlank(message = "Post title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    /**
     * Содержимое создаваемого поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Для body мы устанавливаем более мягкие ограничения по длине,
     * учитывая, что это основной контент поста. В production
     * эти ограничения должны соответствовать требованиям внешнего API.
     * </p>
     */
    @JsonProperty("body")
    @NotBlank(message = "Post body is required")
    @Size(min = 1, max = 5000, message = "Body must be between 1 and 5000 characters")
    private String body;

    /**
     * Создает новый экземпляр CreatePostRequest с валидацией.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Статический factory method предоставляет удобный способ создания
     * объекта с базовой валидацией. Это паттерн, который часто используется
     * в enterprise приложениях для инкапсуляции логики создания объектов.
     * </p>
     *
     * @param userId ID пользователя
     * @param title заголовок поста
     * @param body содержимое поста
     * @return новый экземпляр CreatePostRequest
     * @throws IllegalArgumentException если параметры невалидны
     */
    public static CreatePostRequest of(Long userId, String title, String body) {
        // Базовая валидация перед созданием объекта
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Body cannot be empty");
        }

        return CreatePostRequest.builder()
            .userId(userId)
            .title(title.trim())
            .body(body.trim())
            .build();
    }

    /**
     * Проверяет, готов ли запрос для отправки.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Utility методы для проверки состояния объекта помогают
     * избежать отправки некорректных данных во внешние API.
     * Это дополнительный уровень защиты помимо Bean Validation.
     * </p>
     *
     * @return true, если все обязательные поля заполнены корректно
     */
    public boolean isValid() {
        return userId != null && userId > 0 &&
            title != null && !title.trim().isEmpty() && title.length() <= 100 &&
            body != null && !body.trim().isEmpty() && body.length() <= 5000;
    }

    /**
     * Возвращает краткое описание запроса для логирования.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * При логировании запросов важно не включать полное содержимое,
     * особенно если оно может содержать чувствительные данные.
     * </p>
     *
     * @return краткое описание запроса
     */
    public String toLogString() {
        return String.format("CreatePostRequest{userId=%d, titleLength=%d, bodyLength=%d}",
            userId,
            title != null ? title.length() : 0,
            body != null ? body.length() : 0);
    }

    /**
     * Нормализует данные запроса перед отправкой.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Нормализация данных помогает:
     * </p>
     * <ul>
     *   <li>Убрать лишние пробелы</li>
     *   <li>Привести данные к единому формату</li>
     *   <li>Предотвратить проблемы с кодировкой</li>
     *   <li>Улучшить консистентность данных</li>
     * </ul>
     *
     * @return нормализованная копия объекта
     */
    public CreatePostRequest normalize() {
        return CreatePostRequest.builder()
            .userId(this.userId)
            .title(this.title != null ? this.title.trim() : null)
            .body(this.body != null ? this.body.trim() : null)
            .build();
    }
}