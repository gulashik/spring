package org.gualsh.demo.httpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

/**
 * Модель пользователя из JSONPlaceholder API.
 *
 * <h4>Образовательный момент</h4>
 * <p>
 * Demonstrates best practices for DTO design:
 * </p>
 * <ul>
 * <li>@Data от Lombok для автогенерации методов</li>
 * <li>Validation аннотации для проверки данных</li>
 * <li>Jackson аннотации для кастомного mapping</li>
 * <li>Вложенные классы для структурированных данных</li>
 * </ul>
 *
 * <pre>{@code
 * User user = User.builder()
 *     .id(1L)
 *     .name("John Doe")
 *     .email("john@example.com")
 *     .build();
 * }</pre>
 */
@Data
@Builder
public class User {
    @NotNull
    @Positive
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    private Address address;

    private String phone;

    private String website;

    private Company company;

    /**
     * Адрес пользователя.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Вложенные классы помогают структурировать данные
     * и обеспечивают лучшую читаемость кода.
     * </p>
     */
    @Data
    @Builder
    public static class Address {
        private String street;
        private String suite;
        private String city;
        private String zipcode;
        private Geo geo;

        @Data
        @Builder
        public static class Geo {
            @JsonProperty("lat")
            private String latitude;

            @JsonProperty("lng")
            private String longitude;
        }
    }

    /**
     * Компания пользователя.
     */
    @Data
    @Builder
    public static class Company {
        private String name;

        @JsonProperty("catchPhrase")
        private String catchPhrase;

        @JsonProperty("bs")
        private String businessSector;
    }
}

