package org.gualsh.demo.webclient.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.gualsh.demo.webclient.dto.UserDto;
import org.gualsh.demo.webclient.dto.PostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Простые тесты для JsonPlaceholderService без использования StepVerifier.
 *
 * <p>Демонстрирует альтернативные подходы к тестированию reactive кода:</p>
 * <ul>
 *   <li>Блокирующее получение результатов с timeout</li>
 *   <li>Использование AssertJ для проверок</li>
 *   <li>Простая настройка WireMock</li>
 *   <li>Тестирование без reactor-test зависимости</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
 */
@DisplayName("Simple JsonPlaceholder Service Tests")
class SimpleServiceTest {

    private WireMockServer wireMockServer;
    private JsonPlaceholderService jsonPlaceholderService;
    private WebClient testWebClient;

    /**
     * Настройка перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        // Запускаем WireMock на случайном порту
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();

        // Создаем WebClient для тестов
        testWebClient = WebClient.builder()
            .baseUrl(wireMockServer.baseUrl())
            .build();

        // Создаем сервис с тестовым WebClient
        jsonPlaceholderService = new JsonPlaceholderService(testWebClient, 3, 1000);
    }

    /**
     * Очистка после каждого теста.
     */
    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    /**
     * Простой тест получения всех пользователей.
     *
     * <p>Использует block() для получения результата синхронно.</p>
     */
    @Test
    @DisplayName("Should fetch all users successfully")
    void shouldFetchAllUsers() {
        // Given: настраиваем мок ответ
        String usersJson = """
            [
                {
                    "id": 1,
                    "name": "Leanne Graham",
                    "username": "Bret",
                    "email": "Sincere@april.biz",
                    "phone": "1-770-736-8031 x56442",
                    "website": "hildegard.org"
                },
                {
                    "id": 2,
                    "name": "Ervin Howell",
                    "username": "Antonette",
                    "email": "Shanna@melissa.tv",
                    "phone": "010-692-6593 x09125",
                    "website": "anastasia.net"
                }
            ]
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(usersJson)));

        // When: выполняем запрос и блокируем результат
        List<UserDto> users = jsonPlaceholderService.getAllUsers()
            .block(Duration.ofSeconds(5));

        // Then: проверяем результат
        assertThat(users).isNotNull();
        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(1L);
        assertThat(users.get(0).getUsername()).isEqualTo("Bret");
        assertThat(users.get(1).getId()).isEqualTo(2L);
        assertThat(users.get(1).getUsername()).isEqualTo("Antonette");

        // Проверяем что был сделан правильный HTTP запрос
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users"))
            .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    /**
     * Тест получения пользователя по ID.
     */
    @Test
    @DisplayName("Should fetch user by ID successfully")
    void shouldFetchUserById() {
        // Given
        Long userId = 1L;
        String userJson = """
            {
                "id": 1,
                "name": "Leanne Graham",
                "username": "Bret",
                "email": "Sincere@april.biz"
            }
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(userJson)));

        // When
        UserDto user = jsonPlaceholderService.getUserById(userId)
            .block(Duration.ofSeconds(5));

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("Bret");
        assertThat(user.getEmail()).isEqualTo("Sincere@april.biz");
    }

    /**
     * Тест обработки 404 ошибки.
     */
    @Test
    @DisplayName("Should handle user not found error")
    void shouldHandleUserNotFound() {
        // Given
        Long userId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(404)));

        // When & Then
        try {
            jsonPlaceholderService.getUserById(userId)
                .block(Duration.ofSeconds(5));

            // Если мы дошли до этой точки, тест провалился
            assertThat(false).as("Expected exception was not thrown").isTrue();
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("User not found");
        }
    }

    /**
     * Тест получения постов пользователя.
     */
    @Test
    @DisplayName("Should fetch posts by user ID")
    void shouldFetchPostsByUserId() {
        // Given
        Long userId = 1L;
        String postsJson = """
            [
                {
                    "id": 1,
                    "userId": 1,
                    "title": "Post 1",
                    "body": "Body 1"
                },
                {
                    "id": 2,
                    "userId": 1,
                    "title": "Post 2",
                    "body": "Body 2"
                }
            ]
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/posts"))
            .withQueryParam("userId", equalTo("1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(postsJson)));

        // When: собираем Flux в List
        List<PostDto> posts = jsonPlaceholderService.getPostsByUserId(userId)
            .collectList()
            .block(Duration.ofSeconds(5));

        // Then
        assertThat(posts).isNotNull();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getUserId()).isEqualTo(userId);
        assertThat(posts.get(1).getUserId()).isEqualTo(userId);

        // Проверяем query параметр
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/posts"))
            .withQueryParam("userId", equalTo("1")));
    }

    /**
     * Тест с задержкой и таймаутом.
     */
    @Test
    @DisplayName("Should handle slow response within timeout")
    void shouldHandleSlowResponse() {
        // Given: медленный ответ
        String userJson = """
            {
                "id": 1,
                "name": "Slow User",
                "username": "slowuser",
                "email": "slow@example.com"
            }
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(2000) // 2 секунды задержки
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(userJson)));

        // When: запрос с достаточным таймаутом
        UserDto user = jsonPlaceholderService.getUserById(1L)
            .block(Duration.ofSeconds(5));

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("slowuser");
    }

    /**
     * Тест композиции пользователя с постами.
     */
    @Test
    @DisplayName("Should get user with posts composite data")
    void shouldGetUserWithPosts() {
        // Given: настраиваем моки для пользователя и постов
        String userJson = """
            {
                "id": 1,
                "name": "Test User",
                "username": "testuser",
                "email": "test@example.com"
            }
            """;

        String postsJson = """
            [
                {
                    "id": 1,
                    "userId": 1,
                    "title": "Post 1",
                    "body": "Body 1"
                }
            ]
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(userJson)));

        wireMockServer.stubFor(get(urlPathEqualTo("/posts"))
            .withQueryParam("userId", equalTo("1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(postsJson)));

        // When: получаем композитные данные
        var result = jsonPlaceholderService.getUserWithPosts(1L)
            .block(Duration.ofSeconds(10));

        // Then: проверяем структуру результата
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("user", "posts", "postsCount");

        @SuppressWarnings("unchecked")
        var posts = (List<PostDto>) result.get("posts");
        assertThat(posts).hasSize(1);
        assertThat(result.get("postsCount")).isEqualTo(1);
    }
}