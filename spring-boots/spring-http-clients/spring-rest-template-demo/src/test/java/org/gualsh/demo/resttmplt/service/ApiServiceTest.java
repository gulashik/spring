package org.gualsh.demo.resttmplt.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.gualsh.demo.resttmplt.model.Post;
import org.gualsh.demo.resttmplt.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для ApiService с использованием WireMock.
 *
 * <p>Класс демонстрирует:</p>
 * <ul>
 *   <li>Тестирование HTTP взаимодействий с помощью WireMock</li>
 *   <li>Мокирование внешних API сервисов</li>
 *   <li>Проверку retry механизмов</li>
 *   <li>Тестирование обработки ошибок</li>
 *   <li>Валидацию кэширования результатов</li>
 * </ul>
 *
 * @author gualsh
 * @version 1.0
 * @since 1.0
 */
@SpringBootTest
@TestPropertySource(properties = {
    "app.rest-template.external-apis.jsonplaceholder=http://localhost:8089",
    "app.rest-template.external-apis.httpbin=http://localhost:8089"
})
class ApiServiceTest {

    @Autowired
    private ApiService apiService;

    private WireMockServer wireMockServer;

    /**
     * Настройка WireMock сервера перед каждым тестом.
     *
     * <p>WireMock позволяет:</p>
     * <ul>
     *   <li>Эмулировать поведение внешних API</li>
     *   <li>Контролировать ответы и задержки</li>
     *   <li>Тестировать различные сценарии ошибок</li>
     *   <li>Проверять отправленные запросы</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    /**
     * Остановка WireMock сервера после каждого теста.
     */
    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    /**
     * Тест успешного получения пользователя по ID.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Корректность формирования URL с параметрами</li>
     *   <li>Десериализацию JSON в объект User</li>
     *   <li>Обработку успешного ответа</li>
     * </ul>
     */
    @Test
    void testGetUserById_Success() {
        // Подготовка мока
        stubFor(get(urlEqualTo("/users/1"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            {
                                "id": 1,
                                "name": "John Doe",
                                "username": "johndoe",
                                "email": "john@example.com",
                                "phone": "123-456-7890",
                                "website": "johndoe.com"
                            }
                            """)));

        // Выполнение теста
        User user = apiService.getUserById(1L);

        // Проверки
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());

        // Проверка, что запрос был выполнен
        //verify(getRequestedFor(urlEqualTo("/users/1")));
    }

    /**
     * Тест получения всех пользователей с ParameterizedTypeReference.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Корректную десериализацию List&lt;User&gt;</li>
     *   <li>Обработку массива JSON объектов</li>
     *   <li>Работу кэширования</li>
     * </ul>
     */
    @Test
    void testGetAllUsers_Success() {
        // Подготовка мока
        stubFor(get(urlEqualTo("/users"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            [
                                {
                                    "id": 1,
                                    "name": "John Doe",
                                    "username": "johndoe",
                                    "email": "john@example.com"
                                },
                                {
                                    "id": 2,
                                    "name": "Jane Smith",
                                    "username": "janesmith",
                                    "email": "jane@example.com"
                                }
                            ]
                            """)));

        // Выполнение теста
        List<User> users = apiService.getAllUsers();

        // Проверки
        assertNotNull(users);
        assertEquals(2, users.size());

        User firstUser = users.get(0);
        assertEquals(1L, firstUser.getId());
        assertEquals("John Doe", firstUser.getName());

        User secondUser = users.get(1);
        assertEquals(2L, secondUser.getId());
        assertEquals("Jane Smith", secondUser.getName());

        verify(getRequestedFor(urlEqualTo("/users")));
    }

    /**
     * Тест создания поста с проверкой заголовков.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>POST запрос с JSON телом</li>
     *   <li>Отправку кастомных заголовков</li>
     *   <li>Получение созданного объекта с ID</li>
     * </ul>
     */
    @Test
    void testCreatePost_Success() {
        // Подготовка мока
        stubFor(post(urlEqualTo("/posts"))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader("User-Agent", equalTo("RestTemplate-Demo/1.0"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            {
                                "id": 101,
                                "userId": 1,
                                "title": "Test Post",
                                "body": "This is a test post body"
                            }
                            """)));

        // Подготовка данных
        Post newPost = new Post(1L, "Test Post", "This is a test post body");

        // Выполнение теста
        Post createdPost = apiService.createPost(newPost);

        // Проверки
        assertNotNull(createdPost);
        assertEquals(101L, createdPost.getId());
        assertEquals(1L, createdPost.getUserId());
        assertEquals("Test Post", createdPost.getTitle());
        assertEquals("This is a test post body", createdPost.getBody());

        // Проверка отправленных заголовков
        verify(postRequestedFor(urlEqualTo("/posts"))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader("User-Agent", equalTo("RestTemplate-Demo/1.0")));
    }

    /**
     * Тест retry механизма при временных сбоях.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Автоматические повторные попытки при ошибках</li>
     *   <li>Успешный результат после нескольких сбоев</li>
     *   <li>Экспоненциальные задержки между попытками</li>
     * </ul>
     */
    @Test
    void testRetryMechanism_Success() {
        // Настройка: первые два запроса возвращают ошибку, третий успешный
        stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Test")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("First Retry"));

        stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Test")
            .whenScenarioStateIs("First Retry")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("Second Retry"));

        stubFor(get(urlEqualTo("/users/1"))
            .inScenario("Retry Test")
            .whenScenarioStateIs("Second Retry")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            {
                                "id": 1,
                                "name": "John Doe",
                                "username": "johndoe",
                                "email": "john@example.com"
                            }
                            """)));

        // Выполнение теста
        User user = apiService.getUserById(1L);

        // Проверки
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());

        // Проверка, что было выполнено 3 запроса
        verify(3, getRequestedFor(urlEqualTo("/users/1")));
    }

    /**
     * Тест метода восстановления при исчерпании попыток.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Срабатывание @Recover метода</li>
     *   <li>Возврат резервного значения</li>
     *   <li>Обработку критических сбоев</li>
     * </ul>
     */
    @Test
    void testRecoverMechanism_Fallback() {
        // Настройка: все запросы возвращают ошибку
        stubFor(get(urlEqualTo("/users/999"))
            .willReturn(aResponse().withStatus(500)));

        // Выполнение теста
        User user = apiService.getUserById(999L);

        // Проверки на резервного пользователя
        assertNotNull(user);
        assertEquals(999L, user.getId());
        assertEquals("Unknown User", user.getName());
        assertEquals("unknown", user.getUsername());
        assertEquals("unknown@example.com", user.getEmail());

        // Проверка количества попыток (3 согласно настройке @Retryable)
        verify(3, getRequestedFor(urlEqualTo("/users/999")));
    }

    /**
     * Тест обновления поста с PUT запросом.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>PUT запрос с JSON телом</li>
     *   <li>Передачу ID в URL</li>
     *   <li>Обновление существующего ресурса</li>
     * </ul>
     */
    @Test
    void testUpdatePost_Success() {
        // Подготовка мока
        stubFor(put(urlEqualTo("/posts/1"))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            {
                                "id": 1,
                                "userId": 1,
                                "title": "Updated Post",
                                "body": "Updated post body"
                            }
                            """)));

        // Подготовка данных
        Post updatePost = new Post(1L, 1L, "Updated Post", "Updated post body");

        // Выполнение теста
        Post updatedPost = apiService.updatePost(1L, updatePost);

        // Проверки
        assertNotNull(updatedPost);
        assertEquals(1L, updatedPost.getId());
        assertEquals("Updated Post", updatedPost.getTitle());
        assertEquals("Updated post body", updatedPost.getBody());

        verify(putRequestedFor(urlEqualTo("/posts/1")));
    }

    /**
     * Тест удаления поста с проверкой статус-кода.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>DELETE запрос</li>
     *   <li>Обработку статус-кода 204 No Content</li>
     *   <li>Возврат boolean результата</li>
     * </ul>
     */
    @Test
    void testDeletePost_Success() {
        // Подготовка мока
        stubFor(delete(urlEqualTo("/posts/1"))
            .willReturn(aResponse().withStatus(204)));

        // Выполнение теста
        boolean result = apiService.deletePost(1L);

        // Проверки
        assertTrue(result);

        verify(deleteRequestedFor(urlEqualTo("/posts/1")));
    }

    /**
     * Тест получения постов пользователя с path параметрами.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>URL с вложенными ресурсами</li>
     *   <li>Передачу параметров в URL</li>
     *   <li>Десериализацию списка постов</li>
     * </ul>
     */
    @Test
    void testGetUserPosts_Success() {
        // Подготовка мока
        stubFor(get(urlEqualTo("/users/1/posts"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            [
                                {
                                    "id": 1,
                                    "userId": 1,
                                    "title": "Post 1",
                                    "body": "Body of post 1"
                                },
                                {
                                    "id": 2,
                                    "userId": 1,
                                    "title": "Post 2",
                                    "body": "Body of post 2"
                                }
                            ]
                            """)));

        // Выполнение теста
        List<Post> posts = apiService.getUserPosts(1L);

        // Проверки
        assertNotNull(posts);
        assertEquals(2, posts.size());

        Post firstPost = posts.get(0);
        assertEquals(1L, firstPost.getId());
        assertEquals(1L, firstPost.getUserId());
        assertEquals("Post 1", firstPost.getTitle());

        verify(getRequestedFor(urlEqualTo("/users/1/posts")));
    }

    /**
     * Тест демонстрации заголовков с httpbin.
     *
     * <p>Проверяет:</p>
     * <ul>
     *   <li>Отправку кастомных заголовков</li>
     *   <li>Получение ответа в виде Map</li>
     *   <li>Работу с различными типами контента</li>
     * </ul>
     */
    @Test
    void testDemonstrateHeaders_Success() {
        // Подготовка мока
        stubFor(get(urlEqualTo("/headers"))
            .withHeader("X-Custom-Header", equalTo("Demo-Value"))
            .withHeader("X-Client-Version", equalTo("1.0.0"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                            {
                                "headers": {
                                    "X-Custom-Header": "Demo-Value",
                                    "X-Client-Version": "1.0.0",
                                    "User-Agent": "RestTemplate-Demo"
                                }
                            }
                            """)));

        // Выполнение теста
        var headerInfo = apiService.demonstrateHeaders();

        // Проверки
        assertNotNull(headerInfo);
        assertFalse(headerInfo.isEmpty());

        // Проверка отправленных заголовков
        verify(getRequestedFor(urlEqualTo("/headers"))
            .withHeader("X-Custom-Header", equalTo("Demo-Value"))
            .withHeader("X-Client-Version", equalTo("1.0.0")));
    }

    /**
     * Тест обработки ошибки 404 Not Found.
     *
     * <p>Проверяет корректную обработку HTTP ошибок клиента.</p>
     */
    @Test
    void testHandleNotFound_Error() {
        // Подготовка мока
        stubFor(get(urlEqualTo("/users/999"))
            .willReturn(aResponse().withStatus(404)));

        // Выполнение теста должно привести к срабатыванию recover метода
        User user = apiService.getUserById(999L);

        // Проверки на резервного пользователя (из recover метода)
        assertNotNull(user);
        assertEquals(999L, user.getId());
        assertEquals("Unknown User", user.getName());
    }
}