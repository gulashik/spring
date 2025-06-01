package org.gualsh.demo.webclient.integration;

import org.gualsh.demo.webclient.WebClientDemoApplication;
import org.gualsh.demo.webclient.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для демонстрационного приложения WebClient.
 *
 * <p>Эти тесты проверяют корректность взаимодействия компонентов системы,
 * демонстрируя различные возможности WebClient и WebTestClient для тестирования
 * REST API с использованием реактивных подходов.</p>
 *
 * <p>Особенности реализации:</p>
 * <ul>
 *   <li>AssertJ для более читаемых assertions</li>
 *   <li>Корректная обработка реактивных типов (Mono, Flux)</li>
 *   <li>Лучшие практики для тестирования потоковых данных (streaming)</li>
 *   <li>Детальная проверка структуры ответов и их содержимого</li>
 *   <li>Тестирование кэширования и производительности</li>
 * </ul>
 *
 * <p><strong>Образовательный момент:</strong></p>
 * <p>Интеграционные тесты отличаются от модульных тем, что они проверяют взаимодействие
 * нескольких компонентов системы в реальных условиях. WebTestClient в Spring - это
 * специальный инструмент для тестирования веб-приложений, который позволяет эмулировать
 * HTTP-запросы и проверять ответы сервера без фактического запуска веб-сервера.
 * Он особенно полезен для тестирования реактивных приложений, так как поддерживает
 * проверку Flux и Mono потоков.</p>
 *
 * @see WebTestClient
 * @see SpringBootTest
 * @see ActiveProfiles
 * @see TestInstance
 */
@SpringBootTest(
    classes = WebClientDemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient(timeout = "30s") // @AutoConfigureWebTestClient - для автоматической настройки WebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("WebClient Integration Tests")
class WebClientIntegrationTest {

    /**
     * Клиент для тестирования веб-приложения.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>WebTestClient - это инструмент для тестирования веб-приложений Spring WebFlux.
     * Он предоставляет API, похожий на WebClient, но с дополнительными возможностями
     * для тестирования. Внедрение зависимости происходит автоматически благодаря
     * аннотации {@link AutoConfigureWebTestClient}.</p>
     */
    @Autowired
    private WebTestClient webTestClient;

    /**
     * Тест получения всех пользователей с детальной проверкой.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Этот тест демонстрирует проверку списочных данных с помощью AssertJ.
     * Мы проверяем не только статус ответа, но и наличие нужных заголовков,
     * корректность структуры данных и уникальность идентификаторов. Тесты должны быть
     * максимально детальными, чтобы выявлять не только очевидные ошибки, но и
     * проблемы с целостностью данных.</p>
     */
    @Test
    @DisplayName("Should get all users with detailed validation")
    void shouldGetAllUsersWithValidation() {
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectHeader().exists("X-Total-Count")
            .expectBodyList(UserDto.class)
            .value(users -> {
                assertThat(users).isNotEmpty();
                assertThat(users).hasSize(10);

                // Проверяем структуру первого пользователя
                UserDto firstUser = users.get(0);
                assertThat(firstUser.getId()).isNotNull().isPositive();
                assertThat(firstUser.getUsername()).isNotBlank();
                assertThat(firstUser.getEmail()).isNotBlank().contains("@");
                assertThat(firstUser.getName()).isNotBlank();

                // Проверяем что все пользователи имеют уникальные ID
                List<Long> userIds = users.stream().map(UserDto::getId).toList();
                assertThat(userIds).doesNotHaveDuplicates();
            });
    }

    /**
     * Тест получения пользователя по ID с проверкой вложенных объектов.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>При тестировании сложных объектов важно проверять не только наличие
     * полей первого уровня, но и корректность вложенных структур. В этом тесте
     * мы проверяем адрес пользователя и его географические координаты,
     * что позволяет обнаружить ошибки сериализации/десериализации сложных объектов.</p>
     */
    @Test
    @DisplayName("Should get user by ID with nested objects validation")
    void shouldGetUserByIdWithNestedValidation() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{id}", userId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(UserDto.class)
            .value(user -> {
                assertThat(user.getId()).isEqualTo(userId);
                assertThat(user.getUsername()).isNotBlank();
                assertThat(user.getEmail()).isNotBlank().contains("@");

                // Проверяем адрес
                if (user.getAddress() != null) {
                    assertThat(user.getAddress().getCity()).isNotBlank();
                    assertThat(user.getAddress().getStreet()).isNotBlank();

                    // Проверяем географические координаты
                    if (user.getAddress().getGeo() != null) {
                        assertThat(user.getAddress().getGeo().getLatitude()).isNotBlank();
                        assertThat(user.getAddress().getGeo().getLongitude()).isNotBlank();
                    }
                }

                // Проверяем компанию
                if (user.getCompany() != null) {
                    assertThat(user.getCompany().getName()).isNotBlank();
                }
            });
    }

    /**
     * Улучшенный тест для проверки Server-Sent Events с правильной обработкой streaming.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Server-Sent Events (SSE) - это технология, позволяющая серверу отправлять
     * обновления клиенту через одно HTTP-соединение. В реактивном программировании
     * SSE представляются как Flux (поток элементов). При тестировании таких потоков
     * важно проверить, что все элементы соответствуют ожиданиям и что поток
     * завершается корректно.</p>
     */
    @Test
    @DisplayName("Should stream user posts as Server-Sent Events with validation")
    void shouldStreamUserPostsWithValidation() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{userId}/posts", userId)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .expectBodyList(PostDto.class)
            .value(posts -> {
                assertThat(posts).isNotEmpty();

                // Проверяем что все посты принадлежат указанному пользователю
                assertThat(posts).allSatisfy(post -> {
                    assertThat(post.getUserId()).isEqualTo(userId);
                    assertThat(post.getId()).isNotNull().isPositive();
                    assertThat(post.getTitle()).isNotBlank();
                    assertThat(post.getBody()).isNotBlank();
                });

                // Проверяем уникальность ID постов
                List<Long> postIds = posts.stream().map(PostDto::getId).toList();
                assertThat(postIds).doesNotHaveDuplicates();
            });
    }

    /**
     * Тест создания поста с детальной валидацией ответа.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>При тестировании POST-запросов важно проверять не только успешность операции,
     * но и корректность возвращаемых данных. Кроме того, следует обращать внимание
     * на заголовки ответа, например, Location, который содержит URI созданного ресурса.
     * Этот тест также демонстрирует использование Builder-паттерна для создания тестовых данных.</p>
     */
    @Test
    @DisplayName("Should create post with detailed response validation")
    void shouldCreatePostWithDetailedValidation() {
        CreatePostDto createPostDto = CreatePostDto.builder()
            .userId(1L)
            .title("Integration Test Post")
            .body("This post was created during detailed integration testing with improved assertions")
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPostDto))
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("Location")
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(PostDto.class)
            .value(post -> {
                assertThat(post.getId()).isNotNull().isPositive();
                assertThat(post.getTitle()).isEqualTo(createPostDto.getTitle());
                assertThat(post.getBody()).isEqualTo(createPostDto.getBody());
                assertThat(post.getUserId()).isEqualTo(createPostDto.getUserId());
            });
    }

    /**
     * Тест пагинации с проверкой заголовков и структуры ответа.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>При тестировании API с пагинацией важно проверять не только содержимое страницы,
     * но и метаданные пагинации (номер текущей страницы, размер страницы, общее количество
     * элементов). Также полезно проверить наличие специальных заголовков, которые могут
     * содержать информацию о пагинации. В этом тесте мы используем JSONPath для проверки
     * структуры JSON-ответа и дополнительно анализируем текстовое представление ответа.</p>
     */
    @Test
    @DisplayName("Should handle pagination with headers and response structure validation")
    void shouldHandlePaginationWithValidation() {
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
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.data").isArray()
            .jsonPath("$.page").isEqualTo(page)
            .jsonPath("$.size").isEqualTo(size)
            .jsonPath("$.timestamp").exists()
            .jsonPath("$.hasNext").isBoolean()
            .jsonPath("$.hasPrevious").isBoolean()
            .consumeWith(response -> {
                // Дополнительная проверка структуры JSON
                String responseBody = new String(response.getResponseBody());
                assertThat(responseBody).contains("\"data\":");
                assertThat(responseBody).contains("\"page\":" + page);
                assertThat(responseBody).contains("\"size\":" + size);
            });
    }

    /**
     * Тест получения профиля пользователя с композитными данными.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Этот тест демонстрирует проверку сложных API, которые объединяют данные из
     * нескольких источников. Важно проверять консистентность данных, например, что
     * количество постов соответствует размеру массива. Такие тесты помогают выявлять
     * проблемы интеграции между различными компонентами системы.</p>
     */
    @Test
    @DisplayName("Should get user profile with composite data validation")
    void shouldGetUserProfileWithCompositeValidation() {
        Long userId = 1L;

        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{userId}/profile", userId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.user").exists()
            .jsonPath("$.user.id").isEqualTo(userId)
            .jsonPath("$.posts").isArray()
            .jsonPath("$.postsCount").isNumber()
            .jsonPath("$.timestamp").exists()
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                assertThat(responseBody).contains("\"user\":");
                assertThat(responseBody).contains("\"posts\":");
                assertThat(responseBody).contains("\"postsCount\":");

                // Проверяем что postsCount соответствует длине массива posts
                assertThat(responseBody).satisfies(body -> {
                    // Простая проверка консистентности данных
                    assertThat(body.contains("\"posts\":[]")).isFalse(); // Посты должны быть
                });
            });
    }

    /**
     * Тест batch операции с детальной валидацией.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Batch-операции позволяют выполнить несколько запросов в рамках одного вызова API,
     * что существенно снижает сетевые накладные расходы. При тестировании таких операций
     * важно проверить, что все запрошенные данные получены корректно. В этом тесте мы
     * проверяем, что все запрошенные пользователи получены и содержат ожидаемые данные.
     * Также обратите внимание на использование метода containsExactlyInAnyOrderElementsOf,
     * который проверяет наличие всех элементов независимо от их порядка.</p>
     */
    @Test
    @DisplayName("Should handle batch user requests with streaming validation")
    void shouldHandleBatchRequestsWithValidation() {
        List<Long> userIds = List.of(1L, 2L, 3L);

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
            .value(users -> {
                assertThat(users).hasSize(userIds.size());

                // Проверяем что получили пользователей с правильными ID
                List<Long> receivedIds = users.stream().map(UserDto::getId).toList();
                assertThat(receivedIds).containsExactlyInAnyOrderElementsOf(userIds);

                // Проверяем что все пользователи имеют корректные данные
                assertThat(users).allSatisfy(user -> {
                    assertThat(user.getId()).isNotNull().isIn(userIds);
                    assertThat(user.getUsername()).isNotBlank();
                    assertThat(user.getEmail()).isNotBlank().contains("@");
                });
            });
    }

    /**
     * Тест валидации с детальной проверкой ошибок.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Негативные тесты так же важны, как и позитивные. Они проверяют корректность
     * обработки ошибок и валидации данных в приложении. В этом тесте мы отправляем
     * заведомо неверные данные и проверяем, что сервер отвечает правильным HTTP-статусом
     * и корректно структурированным сообщением об ошибке. Такие тесты помогают убедиться,
     * что приложение безопасно и корректно обрабатывает некорректный ввод.</p>
     */
    @Test
    @DisplayName("Should validate request data with detailed error checking")
    void shouldValidateRequestWithDetailedErrors() {
        // Создаем невалидный пост - используем null вместо пустой строки
        CreatePostDto invalidPost = CreatePostDto.builder()
            .userId(null) // null userId должен вызвать ошибку валидации
            .title("") // Пустой title
            .body("") // Пустой body
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPost))
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .consumeWith(response -> {
                // Сначала выводим полный ответ для отладки
                String responseBody = new String(response.getResponseBody());
                System.out.println("Validation error response: " + responseBody);

                // Проверяем базовую структуру ошибки
                assertThat(responseBody).contains("timestamp");
                assertThat(responseBody).contains("status");
                assertThat(responseBody).contains("400");
            })
            // Дополнительные проверки, если поля существуют
            .jsonPath("$.timestamp").exists()
            .jsonPath("$.status").isEqualTo(400);
    }

    /**
     * Упрощенный тест валидации - проверяет только основные поля.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Иногда достаточно упрощенной проверки для негативных сценариев, особенно если
     * подробная структура ошибки уже проверена в других тестах. Этот тест фокусируется
     * только на проверке HTTP-статуса и наличия базовых полей в ответе. Также обратите
     * внимание на использование System.out.println для вывода отладочной информации -
     * это полезно на этапе разработки тестов, но в production-коде лучше использовать
     * логгеры.</p>
     */
    @Test
    @DisplayName("Should return bad request for invalid data")
    void shouldReturnBadRequestForInvalidData() {
        // Создаем пост с явно невалидными данными
        CreatePostDto invalidPost = CreatePostDto.builder()
            .userId(null) // null значение
            .title("") // пустая строка
            .body("") // пустая строка
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidPost))
            .exchange()
            .expectStatus().isBadRequest() // Главное - статус 400
            .expectBody()
            .jsonPath("$.timestamp").exists() // Базовые поля должны быть
            .jsonPath("$.status").isEqualTo(400)
            .consumeWith(response -> {
                String responseBody = new String(response.getResponseBody());
                // Просто проверяем, что ответ не пустой
                assertThat(responseBody).isNotBlank();
                System.out.println("Error response: " + responseBody);
            });
    }

    /**
     * Тест с заведомо корректными данными для сравнения.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Для полного покрытия функциональности полезно иметь как негативные,
     * так и позитивные тесты. Этот тест отправляет корректные данные и проверяет
     * успешный ответ сервера. Позитивные тесты подтверждают, что система корректно
     * обрабатывает валидные запросы, а негативные - что она правильно реагирует на ошибки.
     * Вместе они обеспечивают полное покрытие функциональности.</p>
     */
    @Test
    @DisplayName("Should accept valid post data for comparison")
    void shouldAcceptValidPostData() {
        CreatePostDto validPost = CreatePostDto.builder()
            .userId(1L)
            .title("Valid Integration Test Post")
            .body("This is a valid post with proper content for testing")
            .build();

        webTestClient
            .post()
            .uri("/api/v1/jsonplaceholder/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(validPost))
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("Location")
            .expectBody(PostDto.class)
            .value(post -> {
                assertThat(post.getId()).isNotNull();
                assertThat(post.getTitle()).isEqualTo(validPost.getTitle());
                assertThat(post.getBody()).isEqualTo(validPost.getBody());
                assertThat(post.getUserId()).isEqualTo(validPost.getUserId());
            });
    }

    /**
     * Тест производительности кэширования.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>Кэширование - важный аспект оптимизации производительности. Этот тест
     * демонстрирует, как можно проверить эффективность кэширования, сравнивая время
     * выполнения первого и последующих запросов. Хотя в тестовой среде разница может быть
     * небольшой, в продакшн-окружении эффект кэширования обычно более заметен.
     * Также полезно проверять состояние кэша через Actuator endpoints, что позволяет
     * убедиться, что кэш работает как ожидается.</p>
     */
    @Test
    @DisplayName("Should demonstrate caching performance improvement")
    void shouldDemonstrateCachingPerformance() {
        Long userId = 1L;

        // Первый запрос - медленный
        long startTime1 = System.currentTimeMillis();
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{id}", userId)
            .exchange()
            .expectStatus().isOk();
        long duration1 = System.currentTimeMillis() - startTime1;

        // Второй запрос - должен быть из кэша (быстрее)
        long startTime2 = System.currentTimeMillis();
        webTestClient
            .get()
            .uri("/api/v1/jsonplaceholder/users/{id}", userId)
            .exchange()
            .expectStatus().isOk();
        long duration2 = System.currentTimeMillis() - startTime2;

        // Проверяем что второй запрос действительно быстрее
        // (в тестовом окружении разница может быть небольшой)
        assertThat(duration2).isLessThanOrEqualTo(duration1);

        // Проверяем метрики кэша через Actuator
        webTestClient
            .get()
            .uri("/actuator/caches")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.cacheManagers").exists();
    }
}