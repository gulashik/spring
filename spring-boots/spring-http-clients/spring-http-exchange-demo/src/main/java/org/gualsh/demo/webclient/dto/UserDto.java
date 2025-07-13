package org.gualsh.demo.webclient.dto;

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

/**
 * DTO для пользователя из JSONPlaceholder API.
 *
 * <p>Представляет структуру пользователя с полной информацией
 * включая адрес, компанию и контактные данные.</p>
 *
 * <p>Аннотации:</p>
 * <ul>
 *   <li>{@link JsonIgnoreProperties} - игнорирует неизвестные поля при десериализации</li>
 *   <li>{@link Data} - генерирует getters, setters, toString, equals, hashCode</li>
 *   <li>{@link Builder} - генерирует builder pattern</li>
 *   <li>{@link NoArgsConstructor}, {@link AllArgsConstructor} - конструкторы</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto implements Serializable {

    /**
     * Уникальный идентификатор пользователя.
     */
    @NotNull
    private Long id;

    /**
     * Имя пользователя для входа в систему.
     */
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    /**
     * Полное имя пользователя.
     */
    @NotBlank
    @Size(max = 100)
    private String name;

    /**
     * Электронная почта пользователя.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Номер телефона пользователя.
     */
    private String phone;

    /**
     * Веб-сайт пользователя.
     */
    private String website;

    /**
     * Адрес пользователя.
     */
    private AddressDto address;

    /**
     * Информация о компании пользователя.
     */
    private CompanyDto company;

    /**
     * DTO для адреса пользователя.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressDto implements Serializable {

        /**
         * Название улицы.
         */
        private String street;

        /**
         * Номер дома/квартиры.
         */
        private String suite;

        /**
         * Город.
         */
        private String city;

        /**
         * Почтовый индекс.
         */
        private String zipcode;

        /**
         * Географические координаты.
         */
        private GeoDto geo;

        /**
         * DTO для географических координат.
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class GeoDto implements Serializable {

            /**
             * Географическая широта.
             */
            @JsonProperty("lat")
            private String latitude;

            /**
             * Географическая долгота.
             */
            @JsonProperty("lng")
            private String longitude;
        }
    }

    /**
     * DTO для информации о компании.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyDto implements Serializable {

        /**
         * Название компании.
         */
        private String name;

        /**
         * Слоган компании.
         */
        @JsonProperty("catchPhrase")
        private String catchPhrase;

        /**
         * Сфера деятельности компании.
         */
        @JsonProperty("bs")
        private String businessSector;
    }
}











