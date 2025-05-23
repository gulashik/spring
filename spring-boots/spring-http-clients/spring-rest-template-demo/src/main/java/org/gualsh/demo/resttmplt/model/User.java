package org.gualsh.demo.resttmplt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Модель пользователя для работы с внешним API JSONPlaceholder.
 *
 * <p>Класс представляет структуру данных пользователя, получаемых от
 * внешнего сервиса. Использует Jackson аннотации для корректного
 * маппинга JSON полей.</p>
 *
 * <p>Аннотации:</p>
 * <ul>
 *   <li>@JsonIgnoreProperties - игнорирует неизвестные поля в JSON</li>
 *   <li>@JsonProperty - явное указание имени поля в JSON</li>
 * </ul>
 */

@NoArgsConstructor // todo Конструктор по умолчанию. Необходим для Jackson десериализации.
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("website")
    private String website;
}