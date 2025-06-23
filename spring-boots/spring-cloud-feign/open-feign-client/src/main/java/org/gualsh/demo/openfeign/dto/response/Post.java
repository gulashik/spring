package org.gualsh.demo.openfeign.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO для представления поста из JSONPlaceholder API.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Этот класс демонстрирует best practices для создания DTO объектов
 * в Spring OpenFeign:
 * </p>
 *
 * <h3>Ключевые аспекты:</h3>
 * <ul>
 *   <li><strong>Lombok аннотации</strong> - сокращение boilerplate кода</li>
 *   <li><strong>Jackson аннотации</strong> - кастомизация JSON сериализации</li>
 *   <li><strong>Bean Validation</strong> - валидация входящих данных</li>
 *   <li><strong>Immutable design</strong> - использование @Builder для создания</li>
 * </ul>
 *
 * <h3>Lombok аннотации:</h3>
 * <ul>
 *   <li><strong>@Data</strong> - генерирует getters, setters, equals, hashCode, toString</li>
 *   <li><strong>@Builder</strong> - паттерн Builder для удобного создания объектов</li>
 *   <li><strong>@NoArgsConstructor</strong> - конструктор без параметров (нужен для Jackson)</li>
 *   <li><strong>@AllArgsConstructor</strong> - конструктор со всеми параметрами</li>
 * </ul>
 *
 * <h3>Jackson особенности:</h3>
 * <ul>
 *   <li><strong>@JsonProperty</strong> - маппинг полей JSON на поля Java</li>
 *   <li>Автоматическая десериализация из JSON в объект</li>
 *   <li>Поддержка различных naming strategies</li>
 * </ul>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Используйте валидационные аннотации для проверки данных</li>
 *   <li>Документируйте назначение каждого поля</li>
 *   <li>Используйте осмысленные имена полей</li>
 *   <li>Группируйте DTO по функциональности (request/response)</li>
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
public class Post {

    /**
     * Уникальный идентификатор поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Использование @NotNull и @Positive обеспечивает валидацию данных
     * на уровне DTO. Это особенно важно при работе с внешними API,
     * где структура данных может измениться.
     * </p>
     */
    @JsonProperty("id")
    @NotNull(message = "Post ID cannot be null")
    @Positive(message = "Post ID must be positive")
    private Long id;

    /**
     * Идентификатор пользователя, создавшего пост.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Поле userId связывает пост с пользователем. В реальных приложениях
     * такие связи часто реализуются через separate API calls или JOIN запросы.
     * </p>
     */
    @JsonProperty("userId")
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long userId;

    /**
     * Заголовок поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @NotBlank проверяет, что строка не null, не пустая и содержит
     * хотя бы один не-пробельный символ. Это более строгая проверка
     * чем @NotNull или @NotEmpty.
     * </p>
     */
    @JsonProperty("title")
    @NotBlank(message = "Post title cannot be blank")
    private String title;

    /**
     * Содержимое поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Для длинных текстовых полей важно учитывать:
     * </p>
     * <ul>
     *   <li>Размер данных при сериализации</li>
     *   <li>Потенциальные проблемы с кодировкой</li>
     *   <li>Валидацию максимальной длины при необходимости</li>
     * </ul>
     */
    @JsonProperty("body")
    @NotBlank(message = "Post body cannot be blank")
    private String body;

    /**
     * Возвращает краткое описание поста для логирования.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Кастомные методы в DTO могут быть полезны для:
     * </p>
     * <ul>
     *   <li>Логирования без чувствительных данных</li>
     *   <li>Форматирования для UI</li>
     *   <li>Вычисляемых полей</li>
     *   <li>Бизнес-логики, специфичной для DTO</li>
     * </ul>
     *
     * @return краткое описание поста
     */
    public String getShortDescription() {
        if (title == null) {
            return "Post #" + id;
        }
        return title.length() > 50 ?
            title.substring(0, 47) + "..." :
            title;
    }

    /**
     * Проверяет, принадлежит ли пост указанному пользователю.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Такие utility методы упрощают бизнес-логику в сервисах
     * и делают код более читаемым и тестируемым.
     * </p>
     *
     * @param userId идентификатор пользователя для проверки
     * @return true, если пост принадлежит указанному пользователю
     */
    public boolean belongsToUser(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}