package org.gualsh.demo.httpe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO модели для демонстрации @HttpExchange.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот файл содержит все DTO модели, используемые в проекте.
 * Показывает различные подходы к созданию DTO:
 * </p>
 * <ul>
 * <li>Простые POJO с Lombok</li>
 * <li>Record классы для неизменяемых данных</li>
 * <li>Builder pattern для сложных объектов</li>
 * <li>Jackson аннотации для JSON mapping</li>
 * <li>Validation аннотации для валидации</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
public class DtoModels {

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
    public static class User {
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

    /**
     * Модель поста из JSONPlaceholder API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Record класс для неизменяемых данных.
     * Преимущества record:
     * </p>
     * <ul>
     * <li>Компактный синтаксис</li>
     * <li>Автоматическая неизменяемость</li>
     * <li>Автогенерация equals/hashCode/toString</li>
     * <li>Безопасность потоков</li>
     * </ul>
     *
     * <pre>{@code
     * Post post = new Post(1L, 1L, "Title", "Content");
     *
     * // Получение данных
     * Long id = post.id();
     * String title = post.title();
     * }</pre>
     */
    public record Post(
        @NotNull @Positive Long id,
        @NotNull @Positive Long userId,
        @NotBlank String title,
        @NotBlank String body
    ) {
        /**
         * Создает новый пост с обновленным заголовком.
         *
         * <h4>Образовательный момент</h4>
         * <p>
         * Поскольку record неизменяемый, для изменения данных
         * нужно создавать новый экземпляр.
         * </p>
         *
         * @param newTitle новый заголовок
         * @return новый пост с обновленным заголовком
         */
        public Post withTitle(String newTitle) {
            return new Post(id, userId, newTitle, body);
        }
    }

    /**
     * Модель комментария из JSONPlaceholder API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Комбинация Lombok и validation аннотаций
     * для создания robust DTO.
     * </p>
     */
    @Data
    @Builder
    public static class Comment {
        @NotNull
        @Positive
        private Long id;

        @NotNull
        @Positive
        private Long postId;

        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String body;
    }

    /**
     * Модель пользователя из ReqRes API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает работу с разными API структурами
     * и использование Jackson аннотаций для mapping.
     * </p>
     */
    @Data
    @Builder
    public static class ReqResUser {
        private Long id;

        @Email
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        private String avatar;

        /**
         * Получает полное имя пользователя.
         *
         * <h4>Образовательный момент</h4>
         * <p>
         * Computed properties в DTO помогают
         * инкапсулировать бизнес-логику.
         * </p>
         *
         * @return полное имя
         */
        public String getFullName() {
            return (firstName != null ? firstName : "") +
                " " +
                (lastName != null ? lastName : "");
        }
    }

    /**
     * Ответ от ReqRes API со списком пользователей.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Wrapper класс для paginated ответов.
     * Такой подход типичен для REST API.
     * </p>
     */
    @Data
    @Builder
    public static class ReqResListResponse<T> {
        private int page;

        @JsonProperty("per_page")
        private int perPage;

        private int total;

        @JsonProperty("total_pages")
        private int totalPages;

        private java.util.List<T> data;

        private Support support;

        @Data
        @Builder
        public static class Support {
            private String url;
            private String text;
        }
    }

    /**
     * Модель для создания нового поста.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Отдельная модель для создания объектов
     * (без ID, который генерируется сервером).
     * </p>
     */
    @Data
    @Builder
    public static class CreatePostRequest {
        @NotNull
        @Positive
        private Long userId;

        @NotBlank
        private String title;

        @NotBlank
        private String body;
    }

    /**
     * Модель для обновления поста.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Отдельная модель для обновления,
     * где все поля опциональны (partial update).
     * </p>
     */
    @Data
    @Builder
    public static class UpdatePostRequest {
        private Long userId;
        private String title;
        private String body;
    }

    /**
     * Общая модель ответа API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Generic wrapper для унификации ответов API.
     * Содержит метаданные о результате операции.
     * </p>
     */
    @Data
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        /**
         * Создает успешный ответ.
         *
         * @param data данные
         * @param <T> тип данных
         * @return успешный ответ
         */
        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        }

        /**
         * Создает ответ с ошибкой.
         *
         * @param message сообщение об ошибке
         * @param <T> тип данных
         * @return ответ с ошибкой
         */
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
}
