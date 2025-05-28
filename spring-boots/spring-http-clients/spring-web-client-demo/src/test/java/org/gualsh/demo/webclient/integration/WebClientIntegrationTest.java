package org.gualsh.demo.webclient.integration;

import org.gualsh.demo.webclient.WebClientDemoApplication;
import org.gualsh.demo.webclient.dto.CreatePostDto;
import org.gualsh.demo.webclient.dto.PostDto;
import org.gualsh.demo.webclient.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;

/**
 * Интеграционные тесты для WebClient демо приложения.
 *
 * <p>Тестирует полную интеграцию всех компонентов:</p>
 * <ul>
 *   <li>Spring Boot автоконфигурацию</li>
 *   <li>WebClient конфигурацию</li>
 *   <li>REST контроллеры</li>
 *   <li>Кэширование и retry механизмы</li>
 * </ul>
 *
 * <p>Использует реальные внешние API для полной проверки работоспособности.</p>
 *
 * @author Demo
 * @version 1.0
 */
@SpringBootTest(
    classes = WebClientDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient(timeout = "30s")
@ActiveProfiles("test")
@DisplayName("WebClient Integration Tests")
class WebClientIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * Тест получения всех пользователей через REST API.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>HTTP 200 ответ</li>
     *   <li>Корректный Content-Type</li>
     *   <li>Структуру JSON ответа</li>
     *   <li>Наличие кэширующих заголовков</li>
     * </ul>
     */
    @Test
    @DisplayName("Should get all users via REST API")
    void shouldGetAllUsers() {
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectHeader().exists("X-Total-Count")
            .expectBodyList(UserDto.class)
            .hasSize(10) // JSONPlaceholder возвращает 10 пользователей
            .consumeWith(response -> {
                // Дополнительные проверки структуры данных
                var users = response.getResponseBody();
                assert users != null;
                assert users.get(0).getId() != null;
                assert users.get(0).getUsername() != null;
                assert users.get(0).getEmail() != null;
            });
    }

    /**
     * Тест получения пользователя по ID.
     */
    @Test
    @DisplayName("Should get user by ID via REST API")
    void shouldGetUserById() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{id}", userId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(UserDto.class)
            .consumeWith(response -> {
                UserDto user = response.getResponseBody();
                assert user != null;
                assert user.getId().equals(userId);
                assert user.getUsername() != null;
            });
    }

    /**
     * Тест обработки несуществующего пользователя.
     */
    @Test
    @DisplayName("Should return 404 for non-existent user")
    void shouldReturn404ForNonExistentUser() {
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/999")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    /**
     * Тест создания нового поста.
     */
    @Test
    @DisplayName("Should create new post via REST API")
    void shouldCreatePost() {
        CreatePostDto createPostDto = CreatePostDto.builder()
            .userId(1L)
            .title("Integration Test Post")
            .body("This post was created during integration testing")
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPostDto))
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("Location")
            .expectBody(PostDto.class)
            .consumeWith(response -> {
                PostDto post = response.getResponseBody();
                assert post != null;
                assert post.getId() != null;
                assert post.getTitle().equals("Integration Test Post");
                assert post.getUserId().equals(1L);
            });
    }

    /**
     * Тест получения постов с пагинацией.
     */
    @Test
    @DisplayName("Should get posts with pagination")
    void shouldGetPostsWithPagination() {
        int page = 0;
        int size = 5;

        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/v1/jsonplaceholder/posts")
                .queryParam("page", page)
                .queryParam("size", size)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Page")
            .expectHeader().exists("X-Size")
            .expectHeader().exists("X-Total")
            .expectBody()
            .jsonPath("$.data").isArray()
            .jsonPath("$.page").isEqualTo(page)
            .jsonPath("$.size").isEqualTo(size);
    }

    /**
     * Тест получения постов пользователя в виде Server-Sent Events.
     */
    @Test
    @DisplayName("Should stream user posts as Server-Sent Events")
    void shouldStreamUserPosts() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{userId}/posts", userId)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .expectBodyList(PostDto.class)
            .consumeWith(response -> {
                var posts = response.getResponseBody();
                assert posts != null;
                assert !posts.isEmpty();
                // Проверяем что все посты принадлежат указанному пользователю
                posts.forEach(post -> {
                    assert post.getUserId().equals(userId);
                });
            });
    }

    /**
     * Тест получения профиля пользователя (композитные данные).
     */
    @Test
    @DisplayName("Should get user profile with posts")
    void shouldGetUserProfile() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{userId}/profile", userId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user").exists()
            .jsonPath("$.posts").isArray()
            .jsonPath("$.postsCount").isNumber()
            .jsonPath("$.timestamp").exists()
            .consumeWith(response -> {
                // Дополнительная проверка структуры ответа
                String responseBody = new String(response.getResponseBody());
                assert responseBody.contains("\"user\":");
                assert responseBody.contains("\"posts\":");
                assert responseBody.contains("\"postsCount\":");
            });
    }

    /**
     * Тест валидации при создании поста.
     */
    @Test
    @DisplayName("Should validate post creation data")
    void shouldValidatePostCreation() {
        // Создаем невалидный пост (пустой title)
        CreatePostDto invalidPost = CreatePostDto.builder()
            .userId(1L)
            .title("") // Пустой title должен вызвать ошибку валидации
            .body("Valid body")
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPost))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.error").isEqualTo("Bad Request");
    }

    /**
     * Тест обработки ошибок внешнего API.
     */
    @Test
    @DisplayName("Should handle external API errors gracefully")
    void shouldHandleExternalApiErrors() {
        // Тестируем с несуществующим endpoint
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/invalid")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest(); // Ожидаем ошибку валидации параметра
    }

    /**
     * Тест batch операции для получения нескольких пользователей.
     */
    @Test
    @DisplayName("Should handle batch user requests")
    void shouldHandleBatchUserRequests() {
        var userIds = java.util.List.of(1L, 2L, 3L);

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/users/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .body(BodyInserters.fromValue(userIds))
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .expectBodyList(UserDto.class)
            .hasSize(3)
            .consumeWith(response -> {
                var users = response.getResponseBody();
                assert users != null;
                // Проверяем что получили пользователей с правильными ID
                var receivedIds = users.stream().map(UserDto::getId).toList();
                assert receivedIds.containsAll(userIds);
            });
    }
}