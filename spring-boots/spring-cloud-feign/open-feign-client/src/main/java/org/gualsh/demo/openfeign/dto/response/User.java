package org.gualsh.demo.openfeign.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO для представления пользователя из JSONPlaceholder API.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Этот класс демонстрирует работу с вложенными объектами и сложными структурами данных
 * в OpenFeign. JSONPlaceholder API возвращает пользователей с вложенными объектами
 * для адреса и компании.
 * </p>
 *
 * <h3>Особенности работы с вложенными объектами:</h3>
 * <ul>
 *   <li><strong>@Valid</strong> - каскадная валидация вложенных объектов</li>
 *   <li><strong>Static inner classes</strong> - организация связанных DTO</li>
 *   <li><strong>Jackson автоматически</strong> обрабатывает вложенную структуру</li>
 * </ul>
 *
 * <h3>Best Practices для сложных DTO:</h3>
 * <ul>
 *   <li>Группируйте связанные поля в отдельные классы</li>
 *   <li>Используйте валидацию на всех уровнях вложенности</li>
 *   <li>Документируйте структуру внешнего API</li>
 *   <li>Предусматривайте возможность изменения структуры API</li>
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
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @JsonProperty("id")
    @NotNull(message = "User ID cannot be null")
    @Positive(message = "User ID must be positive")
    private Long id;

    /**
     * Имя пользователя.
     */
    @JsonProperty("name")
    @NotBlank(message = "User name cannot be blank")
    private String name;

    /**
     * Уникальное имя пользователя для входа.
     */
    @JsonProperty("username")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    /**
     * Электронная почта пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @Email валидация проверяет формат email адреса.
     * Это важно при работе с внешними API, где данные
     * могут быть некорректными.
     * </p>
     */
    @JsonProperty("email")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * Адрес пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @Valid аннотация обеспечивает каскадную валидацию.
     * Валидация будет применена не только к полю address,
     * но и ко всем полям внутри объекта Address.
     * </p>
     */
    @JsonProperty("address")
    @Valid
    private Address address;

    /**
     * Номер телефона пользователя.
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * Веб-сайт пользователя.
     */
    @JsonProperty("website")
    private String website;

    /**
     * Информация о компании пользователя.
     */
    @JsonProperty("company")
    @Valid
    private Company company;

    /**
     * DTO для представления адреса пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Использование static inner classes для вложенных DTO имеет преимущества:
     * </p>
     * <ul>
     *   <li>Логическая группировка связанных классов</li>
     *   <li>Уменьшение количества файлов в проекте</li>
     *   <li>Ясность связи между основным и вложенным объектом</li>
     * </ul>
     *
     * <h3>Альтернативные подходы:</h3>
     * <ul>
     *   <li>Отдельные файлы для каждого DTO (для сложных структур)</li>
     *   <li>Пакет dto.nested для вложенных объектов</li>
     *   <li>Использование Map для динамических структур</li>
     * </ul>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {

        /**
         * Название улицы.
         */
        @JsonProperty("street")
        private String street;

        /**
         * Номер дома или suite.
         */
        @JsonProperty("suite")
        private String suite;

        /**
         * Город.
         */
        @JsonProperty("city")
        @NotBlank(message = "City cannot be blank")
        private String city;

        /**
         * Почтовый индекс.
         */
        @JsonProperty("zipcode")
        private String zipcode;

        /**
         * Географические координаты.
         *
         * <h2>Образовательный момент</h2>
         * <p>
         * Еще один уровень вложенности демонстрирует способность
         * Jackson и OpenFeign работать с глубоко вложенными структурами.
         * </p>
         */
        @JsonProperty("geo")
        @Valid
        private Geo geo;

        /**
         * Возвращает полный адрес в виде строки.
         *
         * @return отформатированный адрес
         */
        public String getFullAddress() {
            StringBuilder sb = new StringBuilder();
            if (street != null) sb.append(street);
            if (suite != null) sb.append(", ").append(suite);
            if (city != null) sb.append(", ").append(city);
            if (zipcode != null) sb.append(" ").append(zipcode);
            return sb.toString();
        }

        /**
         * DTO для географических координат.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Geo {

            /**
             * Географическая широта.
             */
            @JsonProperty("lat")
            private String lat;

            /**
             * Географическая долгота.
             */
            @JsonProperty("lng")
            private String lng;
        }
    }

    /**
     * DTO для представления компании пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Этот класс показывает, как обрабатывать бизнес-информацию
     * из внешних API. Часто такие данные требуют дополнительной
     * обработки или валидации.
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Company {

        /**
         * Название компании.
         */
        @JsonProperty("name")
        @NotBlank(message = "Company name cannot be blank")
        private String name;

        /**
         * Девиз или слоган компании.
         */
        @JsonProperty("catchPhrase")
        private String catchPhrase;

        /**
         * Область деятельности компании.
         */
        @JsonProperty("bs")
        private String bs;

        /**
         * Возвращает краткое описание компании.
         *
         * @return описание компании
         */
        public String getDescription() {
            StringBuilder sb = new StringBuilder();
            if (name != null) sb.append(name);
            if (catchPhrase != null) sb.append(" - ").append(catchPhrase);
            return sb.toString();
        }
    }

    /**
     * Возвращает полное имя пользователя для отображения.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Utility методы в DTO помогают инкапсулировать логику форматирования
     * и представления данных, делая код более читаемым.
     * </p>
     *
     * @return полное имя пользователя
     */
    public String getDisplayName() {
        if (name != null && username != null) {
            return name + " (@" + username + ")";
        }
        return name != null ? name : username;
    }

    /**
     * Проверяет, заполнена ли контактная информация пользователя.
     *
     * @return true, если есть email или телефон
     */
    public boolean hasContactInfo() {
        return (email != null && !email.trim().isEmpty()) ||
            (phone != null && !phone.trim().isEmpty());
    }
}
