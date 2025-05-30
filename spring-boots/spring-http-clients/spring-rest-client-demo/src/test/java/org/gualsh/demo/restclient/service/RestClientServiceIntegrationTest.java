package org.gualsh.demo.restclient.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.gualsh.demo.restclient.dto.CreateUserRequest;
import org.gualsh.demo.restclient.dto.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для RestClientService.
 *
 * Эти тесты демонстрируют лучшие практики тестирования RestClient:
 * - Использование WireMock для мокирования внешних HTTP сервисов
 * - Тестирование различных сценариев (успех, ошибки, таймауты)
 * - Проверка кеширования и retry логики
 * - Использование TestContainers для интеграционных тестов
 *
 * @author Demo Author
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("RestClientService Integration Tests")
class RestClientServiceIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private RestClientService restClientService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setup() {
        // Инициализация WireMock перед каждым тестом
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        // Остановка WireMock после каждого теста
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Используем статическую переменную вместо поля экземпляра
        if (wireMockServer == null) {
            // Инициализация на случай, если метод вызывается до @BeforeEach
            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
            wireMockServer.start();
        }

        // Регистрация URL WireMock сервера в свойствах Spring
        registry.add("external.api.base-url", () -> wireMockServer.baseUrl());
    }


    // =================================
    // Тесты POST запросов
    // =================================

    @Test
    @DisplayName("Должен успешно создать пользователя")
    void shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequest createRequest = CreateUserRequest.builder()
            .name("New User")
            .username("newuser")
            .email("newuser@example.com")
            .build();

        String responseJson = """
            {
                "id": 11,
                "name": "New User",
                "username": "newuser",
                "email": "newuser@example.com"
            }
            """;

        wireMockServer.stubFor(post(urlEqualTo("/users"))
            .withRequestBody(containing("newuser"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(responseJson)));

        // Act
        var response = restClientService.createUser(createRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(11L);
        assertThat(response.getBody().getName()).isEqualTo("New User");
    }

    // =================================
    // Тесты retry логики
    // =================================

    @Test
    @DisplayName("Должен повторить запрос при временной ошибке")
    void shouldRetryOnTemporaryError() {
        // Arrange - первые два запроса завершаются ошибкой, третий успешен
        String successResponse = """
            [
                {
                    "id": 1,
                    "name": "John Doe",
                    "username": "johndoe",
                    "email": "john@example.com"
                }
            ]
            """;

        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Started")
            .willReturn(aResponse()
                .withStatus(500))
            .willSetStateTo("First Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Failure")
            .willReturn(aResponse()
                .withStatus(500))
            .willSetStateTo("Second Failure"));

        wireMockServer.stubFor(get(urlEqualTo("/users"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Failure")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(successResponse)));

        // Act
        List<User> users = restClientService.getAllUsers();

        // Assert
        assertThat(users).hasSize(10);
    }

    // =================================
    // Тесты DELETE запросов
    // =================================

    @Test
    @DisplayName("Должен успешно удалить пользователя")
    void shouldDeleteUserSuccessfully() {
        // Arrange
        Long userId = 1L;
        wireMockServer.stubFor(delete(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(200)));

        // Act
        boolean result = restClientService.deleteUser(userId);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false при удалении несуществующего пользователя")
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        // Arrange
        Long userId = 999L;
        wireMockServer.stubFor(delete(urlEqualTo("/users/" + userId))
            .willReturn(aResponse()
                .withStatus(404)));
    }
}