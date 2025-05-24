package org.gualsh.demo.restclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO для представления пользователя из JSONPlaceholder API.
 *
 * Этот класс демонстрирует:
 * - Маппинг JSON полей с помощью Jackson аннотаций
 * - Валидацию данных с помощью Bean Validation
 * - Игнорирование неизвестных полей для обратной совместимости
 * - Использование Lombok для генерации boilerplate кода
 *
 * @author Demo Author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    /**
     * Уникальный идентификатор пользователя.
     */
    @NotNull(message = "ID пользователя не может быть null")
    private Long id;

    /**
     * Имя пользователя.
     */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 100, message = "Имя пользователя не может превышать 100 символов")
    private String name;

    /**
     * Уникальное имя пользователя для входа.
     */
    @NotBlank(message = "Username не может быть пустым")
    @Size(max = 50, message = "Username не может превышать 50 символов")
    private String username;

    /**
     * Email адрес пользователя.
     */
    @Email(message = "Некорректный формат email")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    /**
     * Адрес пользователя.
     */
    private Address address;

    /**
     * Номер телефона.
     */
    private String phone;

    /**
     * Веб-сайт пользователя.
     */
    private String website;

    /**
     * Информация о компании пользователя.
     */
    private Company company;

    /**
     * Время создания записи (добавлено для демонстрации).
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * DTO для представления адреса пользователя.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address implements Serializable {
        private String street;
        private String suite;
        private String city;
        private String zipcode;
        private Geo geo;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Geo implements Serializable {
            @JsonProperty("lat")
            private String latitude;

            @JsonProperty("lng")
            private String longitude;
        }
    }

    /**
     * DTO для представления информации о компании.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Company implements Serializable {
        private String name;

        @JsonProperty("catchPhrase")
        private String catchPhrase;

        @JsonProperty("bs")
        private String businessStrategy;
    }
}
