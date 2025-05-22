package org.gualsh.demo.resttmplt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Objects;

/**
 * Модель поста для работы с внешним API JSONPlaceholder.
 *
 * <p>Класс представляет структуру поста блога, используемую для
 * демонстрации различных HTTP операций с RestTemplate.</p>
 *
 * <p>Поддерживает операции:</p>
 * <ul>
 *   <li>Получение постов (GET)</li>
 *   <li>Создание постов (POST)</li>
 *   <li>Обновление постов (PUT/PATCH)</li>
 *   <li>Удаление постов (DELETE)</li>
 * </ul>
 */
@NoArgsConstructor // Конструктор по умолчанию. Необходим для Jackson десериализации.
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {

    /**
     * Уникальный идентификатор поста.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Идентификатор пользователя, создавшего пост.
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * Заголовок поста.
     */
    @JsonProperty("title")
    private String title;

    /**
     * Содержимое поста.
     */
    @JsonProperty("body")
    private String body;

    /**
     * Конструктор с параметрами.
     *
     * @param userId идентификатор пользователя
     * @param title заголовок поста
     * @param body содержимое поста
     */
    public Post(Long userId, String title, String body) {
        this.userId = userId;
        this.title = title;
        this.body = body;
    }
}