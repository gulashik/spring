package org.gualsh.demo.webclient.validation;

import org.gualsh.demo.webclient.WebClientDemoApplication;
import org.gualsh.demo.webclient.dto.CreatePostDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * Специальные тесты для проверки валидации данных.
 *
 * <p>Проверяет работу Bean Validation в Spring WebFlux контексте
 * и корректность обработки ошибок валидации.</p>
 *
 * @author Demo
 * @version 1.0
 */
@SpringBootTest(
    classes = WebClientDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient(timeout = "10s")
@ActiveProfiles("test")
@DisplayName("Validation Tests")
class ValidationTest {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * Тест отладки - проверяет что возвращает API при невалидных данных.
     */
    @Test
    @DisplayName("Debug validation response structure")
    void debugValidationResponse() {
        // Тест с явно невалидными данными
        CreatePostDto invalidPost = CreatePostDto.builder()
            .userId(null) // NotNull валидация
            .title("") // NotBlank + Size валидация
            .body("") // NotBlank + Size валидация
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPost))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== VALIDATION ERROR DEBUG ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Headers: " + response.getResponseHeaders());
                System.out.println("Body: " + responseBody);
                System.out.println("================================");
            });
    }

    /**
     * Тест с JSON строкой напрямую.
     */
    @Test
    @DisplayName("Test validation with raw JSON")
    void testValidationWithRawJson() {
        String invalidJson = """
            {
                "title": "",
                "body": ""
            }
            """;

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidJson)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== RAW JSON VALIDATION DEBUG ===");
                System.out.println("Body: " + responseBody);
                System.out.println("==================================");
            });
    }

    /**
     * Тест с полностью отсутствующими полями.
     */
    @Test
    @DisplayName("Test validation with missing fields")
    void testValidationWithMissingFields() {
        String emptyJson = "{}";

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(emptyJson)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== MISSING FIELDS VALIDATION DEBUG ===");
                System.out.println("Body: " + responseBody);
                System.out.println("========================================");
            });
    }

    /**
     * Тест с невалидным JSON.
     */
    @Test
    @DisplayName("Test with malformed JSON")
    void testWithMalformedJson() {
        String malformedJson = "{ invalid json }";

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(malformedJson)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== MALFORMED JSON DEBUG ===");
                System.out.println("Body: " + responseBody);
                System.out.println("=============================");
            });
    }

    /**
     * Тест с корректными данными для сравнения.
     */
    @Test
    @DisplayName("Test with valid data for comparison")
    void testWithValidData() {
        CreatePostDto validPost = CreatePostDto.builder()
            .userId(1L)
            .title("Valid Test Post")
            .body("This is a valid post with proper content")
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(validPost))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== VALID POST SUCCESS DEBUG ===");
                System.out.println("Status: " + response.getStatus());
                System.out.println("Body: " + responseBody);
                System.out.println("=================================");
            });
    }

    /**
     * Проверка работы контроллера без валидации.
     */
    @Test
    @DisplayName("Test endpoint without validation")
    void testEndpointWithoutValidation() {
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                System.out.println("=== GET USER SUCCESS DEBUG ===");
                System.out.println("Body: " + responseBody);
                System.out.println("===============================");
            });
    }
}
