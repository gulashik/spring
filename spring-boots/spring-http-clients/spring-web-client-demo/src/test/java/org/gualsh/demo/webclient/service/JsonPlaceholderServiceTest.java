package org.gualsh.demo.webclient.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.gualsh.demo.webclient.dto.UserDto;
import org.gualsh.demo.webclient.dto.PostDto;
import org.gualsh.demo.webclient.dto.CreatePostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Тесты для JsonPlaceholderService с использованием WireMock.
 *
 * <p>Демонстрирует лучшие практики тестирования WebClient:</p>
 * <ul>
 *   <li>Использование WireMock для моков HTTP сервисов</li>
 *   <li>StepVerifier для тестирования reactive streams</li>
 *   <li>Тестирование различных сценариев (успех, ошибки, таймауты)</li>
 *   <li>Проверка retry механизмов</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonPlaceholderService Tests")
class JsonPlaceholderServiceTest {

    private WireMockServer wireMockServer;
    private JsonPlaceholderService jsonPlaceholderService;
    private WebClient testWebClient;

    /**
     * Настройка перед каждым тестом.
     *
     * <p>Инициализирует WireMock сервер и создает тестовый WebClient.</p>
     */
    @BeforeEach
    void setUp() {
        // Запускаем WireMock на случайном порту
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();

        // Создаем WebClient для тестов, указывающий на WireMock
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
     * Тест успешного получения всех пользователей.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Корректную десериализацию JSON в List&lt;UserDto&gt;</li>
     *   <li>Правильные HTTP заголовки</li>
     *   <li>Обработку ParameterizedTypeReference</li>
     * </ul>
     */
    @Test
    @DisplayName("Should successfully fetch all users")
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
                    "website": "hildegard.org",
                    "address": {
                        "street": "Kulas Light",
                        "suite": "Apt. 556",
                        "city": "Gwenborough",
                        "zipcode": "92998-3874",
                        "geo": {
                            "lat": "-37.3159",
                            "lng": "81.1496"
                        }
                    },
                    "company": {
                        "name": "Romaguera-Crona",
                        "catchPhrase": "Multi-layered client-server neural-net",
                        "bs": "harness real-time e-markets"
                    }
                }
            ]
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(usersJson)));

        // When & Then: выполняем запрос и проверяем результат
        StepVerifier.create(jsonPlaceholderService.getAllUsers())
            .expectNextMatches(users -> {
                // Проверяем что получили список с одним пользователем
                return users.size() == 1 &&
                    users.get(0).getId().equals(1L) &&
                    users.get(0).getUsername().equals("Bret") &&
                    users.get(0).getEmail().equals("Sincere@april.biz");
            })
            .verifyComplete();

        // Проверяем что был сделан правильный HTTP запрос
        wireMockServer.verify(getRequestedFor(urlEqualTo("/users"))
            .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    /**
     * Тест получения пользователя по ID.
     */
    @Test
    @DisplayName("Should successfully fetch user by ID")
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

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getUserById(userId))
            .expectNextMatches(user ->
                user.getId().equals(1L) &&
                    user.getUsername().equals("Bret"))
            .verifyComplete();
    }

    /**
     * Тест обработки 404 ошибки при поиске несуществующего пользователя.
     */
    @Test
    @DisplayName("Should handle user not found (404)")
    void shouldHandleUserNotFound() {
        // Given
        Long userId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(404)));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getUserById(userId))
            .expectErrorMatches(throwable ->
                throwable instanceof RuntimeException &&
                    throwable.getMessage().contains("User not found"))
            .verify();
    }

    /**
     * Тест retry механизма при серверных ошибках.
     *
     * <p>Проверяет что сервис повторяет запросы при 5xx ошибках.</p>
     */
    @Test
    @DisplayName("Should retry on server errors")
    void shouldRetryOnServerErrors() {
        // Given: первые два запроса вернут 500, третий - успех
        String userJson = """
            {
                "id": 1,
                "name": "Test User",
                "username": "testuser",
                "email": "test@example.com"
            }
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("First Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Failure")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("Second Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Failure")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(userJson)));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getUserById(1L))
            .expectNextMatches(user -> user.getUsername().equals("testuser"))
            .verifyComplete();

        // Проверяем что было сделано 3 запроса
        wireMockServer.verify(3, getRequestedFor(urlEqualTo("/users/1")));
    }

    /**
     * Тест создания нового поста.
     */
    @Test
    @DisplayName("Should successfully create post")
    void shouldCreatePost() {
        // Given
        CreatePostDto createPostDto = CreatePostDto.builder()
            .userId(1L)
            .title("Test Post")
            .body("This is a test post")
            .build();

        String responseJson = """
            {
                "id": 101,
                "userId": 1,
                "title": "Test Post",
                "body": "This is a test post"
            }
            """;

        wireMockServer.stubFor(post(urlEqualTo("/posts"))
            .withRequestBody(containing("Test Post"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseJson)));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.createPost(createPostDto))
            .expectNextMatches(post ->
                post.getId().equals(101L) &&
                    post.getTitle().equals("Test Post"))
            .verifyComplete();
    }

    /**
     * Тест получения постов пользователя с query параметрами.
     */
    @Test
    @DisplayName("Should fetch posts by user ID with query parameters")
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

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getPostsByUserId(userId))
            .expectNextCount(2)
            .verifyComplete();

        // Проверяем что запрос содержал правильный query параметр
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/posts"))
            .withQueryParam("userId", equalTo("1")));
    }

    /**
     * Тест таймаута запроса.
     */
    @Test
    @DisplayName("Should handle request timeout")
    void shouldHandleTimeout() {
        // Given: медленный ответ (больше таймаута)
        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(6000) // 6 секунд задержки
                .withBody("{}")));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getUserById(1L))
            .expectTimeout(Duration.ofSeconds(5))
            .verify();
    }

    /**
     * Тест обработки невалидного JSON.
     */
    @Test
    @DisplayName("Should handle invalid JSON response")
    void shouldHandleInvalidJson() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("invalid json")));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getAllUsers())
            .expectError()
            .verify();
    }

    /**
     * Тест удаления поста.
     */
    @Test
    @DisplayName("Should successfully delete post")
    void shouldDeletePost() {
        // Given
        Long postId = 1L;

        wireMockServer.stubFor(delete(urlEqualTo("/posts/" + postId))
            .willReturn(aResponse()
                .withStatus(200)));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.deletePost(postId))
            .verifyComplete();

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/posts/" + postId)));
    }
}