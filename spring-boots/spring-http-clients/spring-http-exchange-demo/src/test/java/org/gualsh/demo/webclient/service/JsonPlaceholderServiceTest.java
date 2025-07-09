package org.gualsh.demo.webclient.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.CreatePostDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.Duration;

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
 * <p><strong>Образовательный момент:</strong></p>
 * <p>WireMock предоставляет изолированную среду для тестирования HTTP-клиентов.
 * Это позволяет имитировать различные сценарии, включая задержки, ошибки и
 * разные форматы ответов, без реального обращения к внешним сервисам. Такой подход
 * обеспечивает стабильность и предсказуемость тестов, поскольку внешние сервисы
 * могут быть недоступны или иметь непредсказуемое поведение.</p>
 * 
 * <p>Для тестирования реактивных потоков используется StepVerifier, который
 * позволяет контролировать и проверять асинхронное выполнение операций.</p>
 *
 * @see com.github.tomakehurst.wiremock.WireMockServer
 * @see reactor.test.StepVerifier
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonPlaceholderService Tests")
class JsonPlaceholderServiceTest {

    /**
     * Сервер WireMock для имитации HTTP-сервиса.
     * 
     * <p>Образовательный момент:</p>
     * <p>WireMockServer создает локальный HTTP-сервер, который может быть
     * настроен для имитации любых HTTP-ответов. Запуск на случайном порту (0)
     * предотвращает конфликты с другими сервисами.</p>
     */
    private WireMockServer wireMockServer;
    
    /**
     * Тестируемый сервис.
     */
    private JsonPlaceholderService jsonPlaceholderService;
    
    /**
     * WebClient для тестирования, настроенный на WireMock.
     * 
     * <p>Образовательный момент:</p>
     * <p>Использование отдельного экземпляра WebClient для тестов позволяет
     * изолировать тесты от реальных сервисов и гарантировать, что запросы
     * идут только на локальный WireMockServer.</p>
     */
    private WebClient testWebClient;

    /**
     * Настройка перед каждым тестом.
     *
     * <p>Инициализирует WireMock сервер и создает тестовый WebClient.</p>
     * 
     * <p>Образовательный момент:</p>
     * <p>Настройка перед каждым тестом обеспечивает изоляцию тестов друг от друга.
     * Каждый тест получает "чистый" сервер без остаточных стабов от предыдущих тестов.</p>
     */
    @BeforeEach
    void setUp() {
        // Запускаем WireMock на случайном порту
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        
        // Конфигурируем WireMock для текущего потока
        WireMock.configureFor("localhost", wireMockServer.port());

        // Создаем WebClient для тестов, указывающий на WireMock
        testWebClient = WebClient.builder()
            .baseUrl(wireMockServer.baseUrl())
            .build();

        // Создаем сервис с тестовым WebClient
        jsonPlaceholderService = new JsonPlaceholderService(testWebClient, 3, 1000);
    }

    /**
     * Очистка после каждого теста.
     * 
     * <p>Образовательный момент:</p>
     * <p>Важно останавливать WireMockServer после каждого теста, чтобы освободить 
     * системные ресурсы и порт. Это предотвращает утечки ресурсов и конфликты 
     * между последовательными запусками тестов.</p>
     * <p>Использование try-catch внутри метода tearDown гарантирует, что даже 
     * при возникновении ошибки во время остановки сервера, другие ресурсы будут 
     * корректно освобождены, а тест получит информацию о проблеме.</p>
     */
    @AfterEach
    void tearDown() {
        try {
            if (wireMockServer != null) {
                if (wireMockServer.isRunning()) {
                    wireMockServer.stop();
                }
                wireMockServer.resetAll(); // Сбрасываем все стабы и сценарии
            }
        } catch (Exception e) {
            // Логируем исключение, но не прерываем процесс очистки
            System.err.println("Ошибка при остановке WireMockServer: " + e.getMessage());
            e.printStackTrace();
            
            // В реальном приложении здесь лучше использовать Logger
             log.error("Ошибка при остановке WireMockServer", e);
        } finally {
            // Дополнительная очистка ресурсов, если необходимо
            wireMockServer = null; // Явно освобождаем ссылку
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
     * 
     * <p>Образовательный момент:</p>
     * <p>Данный тест демонстрирует паттерн Given-When-Then, который делает тесты
     * более читаемыми и понятными. В секции Given настраивается мок ответа,
     * в When выполняется тестируемое действие, а Then проверяет результат.</p>
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
                .withStatus(HttpStatus.OK.value())
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
     * 
     * <p>Образовательный момент:</p>
     * <p>В этом тесте проверяется получение конкретного пользователя по ID.
     * WireMock настраивается на возврат данных только для определенного URL,
     * что позволяет проверить, что клиент правильно формирует URL с параметрами пути.</p>
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
                .withStatus(HttpStatus.OK.value())
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
     * 
     * <p>Образовательный момент:</p>
     * <p>Важной частью тестирования HTTP-клиентов является проверка
     * обработки ошибок. Этот тест проверяет, что сервис правильно обрабатывает
     * ответ 404 (Not Found) и преобразует его в соответствующее исключение.</p>
     */
    @Test
    @DisplayName("Should handle user not found (404)")
    void shouldHandleUserNotFound() {
        // Given
        Long userId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())));

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
     * 
     * <p>Образовательный момент:</p>
     * <p>WireMock позволяет моделировать сложные сценарии с изменением
     * поведения между запросами. В этом тесте используется механизм сценариев
     * WireMock для имитации ситуации, когда первые два запроса завершаются с
     * ошибкой 500, а третий успешен. Это позволяет проверить логику повторных
     * попыток (retry) в клиенте.</p>
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
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .willSetStateTo("First Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Failure")
            .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .willSetStateTo("Second Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Failure")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
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
     * 
     * <p>Образовательный момент:</p>
     * <p>Этот тест демонстрирует проверку POST-запросов с JSON-телом.
     * WireMock позволяет проверить не только URL и заголовки, но и содержимое
     * тела запроса с помощью различных матчеров, например, containing().</p>
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
                .withStatus(HttpStatus.CREATED.value())
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
     * 
     * <p>Образовательный момент:</p>
     * <p>В этом тесте демонстрируется работа с URL-параметрами запроса (query parameters).
     * WireMock позволяет настроить стаб, который будет отвечать только на запросы
     * с определенными параметрами, что позволяет проверить правильность их передачи
     * клиентом.</p>
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
            .withQueryParam("userId", equalTo(userId.toString()))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(postsJson)));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getPostsByUserId(userId))
            .expectNextCount(2)
            .verifyComplete();

        // Проверяем что запрос содержал правильный query параметр
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/posts"))
            .withQueryParam("userId", equalTo(userId.toString())));
    }

    /**
     * Тест таймаута запроса.
     * 
     * <p>Образовательный момент:</p>
     * <p>WireMock позволяет имитировать задержки в ответах сервера с помощью
     * метода withFixedDelay(). Это особенно полезно для тестирования обработки
     * таймаутов и других временных аспектов HTTP-взаимодействия.</p>
     * <p>StepVerifier.expectTimeout() позволяет проверить, что операция
     * прерывается по таймауту в указанный интервал времени.</p>
     */
    @Test
    @DisplayName("Should handle request timeout")
    void shouldHandleTimeout() {
        // Given: медленный ответ (больше таймаута)
        wireMockServer.stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withFixedDelay(6000) // 6 секунд задержки
                .withBody("{}")));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getUserById(1L))
            .expectTimeout(Duration.ofSeconds(5))
            .verify();
    }

    /**
     * Тест обработки невалидного JSON.
     * 
     * <p>Образовательный момент:</p>
     * <p>Этот тест проверяет обработку некорректных ответов от сервера.
     * Несмотря на успешный HTTP-статус (200), тело ответа содержит невалидный JSON,
     * что должно привести к ошибке десериализации. Такие тесты важны для проверки
     * устойчивости приложения к некорректным данным.</p>
     */
    @Test
    @DisplayName("Should handle invalid JSON response")
    void shouldHandleInvalidJson() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("invalid json")));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.getAllUsers())
            .expectError()
            .verify();
    }

    /**
     * Тест удаления поста.
     * 
     * <p>Образовательный момент:</p>
     * <p>Этот тест демонстрирует проверку DELETE-запросов. Особенность DELETE-запросов
     * в том, что они часто возвращают пустое тело (или 204 No Content). Здесь
     * проверяется, что запрос был сделан на правильный URL, а результирующий Mono
     * успешно завершается.</p>
     */
    @Test
    @DisplayName("Should successfully delete post")
    void shouldDeletePost() {
        // Given
        Long postId = 1L;

        wireMockServer.stubFor(delete(urlEqualTo("/posts/" + postId))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())));

        // When & Then
        StepVerifier.create(jsonPlaceholderService.deletePost(postId))
            .verifyComplete();

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/posts/" + postId)));
    }
}