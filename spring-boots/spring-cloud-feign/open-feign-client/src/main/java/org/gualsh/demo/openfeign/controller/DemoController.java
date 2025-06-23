package org.gualsh.demo.openfeign.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.openfeign.dto.request.CreatePostRequest;
import org.gualsh.demo.openfeign.dto.response.Post;
import org.gualsh.demo.openfeign.dto.response.User;
import org.gualsh.demo.openfeign.service.DemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST контроллер для демонстрации OpenFeign функциональности.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Этот контроллер предоставляет REST API для тестирования и демонстрации
 * всех возможностей OpenFeign клиентов. Он показывает как правильно
 * структурировать контроллеры, которые используют внешние API.
 * </p>
 *
 * <h3>Архитектурные принципы:</h3>
 * <ul>
 *   <li><strong>Thin Controllers</strong> - контроллер только маршрутизирует запросы</li>
 *   <li><strong>Service Layer</strong> - вся бизнес-логика в сервисах</li>
 *   <li><strong>Error Handling</strong> - централизованная обработка ошибок</li>
 *   <li><strong>Validation</strong> - валидация входных данных</li>
 * </ul>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Используйте ResponseEntity для контроля HTTP статусов</li>
 *   <li>Валидируйте входные параметры</li>
 *   <li>Логируйте важные операции</li>
 *   <li>Документируйте API endpoints</li>
 *   <li>Обрабатывайте edge cases (null, empty данные)</li>
 * </ul>
 *
 * @author Generated for educational purposes
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    /**
     * Получение всех постов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Простейший endpoint, демонстрирующий проксирование внешнего API
     * через внутренний REST endpoint.
     * </p>
     *
     * @return список всех постов
     */
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        log.info("GET /api/demo/posts - fetching all posts");

        try {
            List<Post> posts = demoService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("Failed to fetch posts", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Получение поста по ID.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует обработку path parameters и возврат различных
     * HTTP статусов в зависимости от результата.
     * </p>
     *
     * @param id идентификатор поста
     * @return пост с указанным ID или 404
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable @Positive Long id) {
        log.info("GET /api/demo/posts/{} - fetching post by ID", id);

        try {
            Optional<Post> post = demoService.getPostById(id);

            if (post.isPresent()) {
                return ResponseEntity.ok(post.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch post with ID {}", id, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Создание нового поста.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает обработку POST запросов с валидацией тела запроса
     * и возврат соответствующих статус кодов.
     * </p>
     *
     * @param request данные для создания поста
     * @return созданный пост или ошибка валидации
     */
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody @Valid CreatePostRequest request) {
        log.info("POST /api/demo/posts - creating new post");

        try {
            Post createdPost = demoService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid post data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to create post", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Получение постов конкретного пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует композицию нескольких операций и обработку
     * связанных ресурсов.
     * </p>
     *
     * @param userId идентификатор пользователя
     * @return список постов пользователя
     */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable @Positive Long userId) {
        log.info("GET /api/demo/users/{}/posts - fetching user posts", userId);

        try {
            List<Post> userPosts = demoService.getUserPosts(userId);
            return ResponseEntity.ok(userPosts);
        } catch (IllegalArgumentException e) {
            log.warn("User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to fetch posts for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Получение пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return пользователь с указанным ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable @Positive Long id) {
        log.info("GET /api/demo/users/{} - fetching user by ID", id);

        try {
            Optional<User> user = demoService.getUserById(id);

            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user with ID {}", id, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Демонстрация возможностей HttpBin API.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Этот endpoint показывает различные возможности OpenFeign
     * для работы с HTTP запросами, заголовками и форматами данных.
     * </p>
     *
     * @return результаты тестирования различных HTTP возможностей
     */
    @GetMapping("/httpbin/demo")
    public ResponseEntity<Map<String, Object>> demonstrateHttpBin() {
        log.info("GET /api/demo/httpbin/demo - demonstrating HttpBin features");

        try {
            Map<String, Object> results = demoService.demonstrateHttpBinFeatures();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to demonstrate HttpBin features", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Failed to complete demonstration"));
        }
    }

    /**
     * Тестирование обработки HTTP ошибок.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Позволяет протестировать как ваш ErrorDecoder обрабатывает
     * различные HTTP статус коды от внешних API.
     * </p>
     *
     * @param statusCode HTTP статус код для тестирования
     * @return результат обработки указанного статус кода
     */
    @GetMapping("/httpbin/status/{statusCode}")
    public ResponseEntity<Map<String, Object>> testErrorHandling(
        @PathVariable @Min(100) @Max(599) int statusCode) {

        log.info("GET /api/demo/httpbin/status/{} - testing error handling", statusCode);

        Map<String, Object> result = demoService.testErrorHandling(statusCode);

        // Возвращаем всегда 200, так как мы тестируем обработку ошибок
        // Фактический результат содержится в теле ответа
        return ResponseEntity.ok(result);
    }

    /**
     * Тестирование таймаутов и задержек.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Позволяет протестировать поведение приложения при медленных
     * ответах от внешних сервисов и проверить настройки таймаутов.
     * </p>
     *
     * @param seconds количество секунд задержки (максимум 10)
     * @return результат теста таймаута
     */
    @GetMapping("/httpbin/delay/{seconds}")
    public ResponseEntity<Map<String, Object>> testTimeout(
        @PathVariable @Min(1) @Max(10) int seconds) {

        log.info("GET /api/demo/httpbin/delay/{} - testing timeout", seconds);

        Map<String, Object> result = demoService.testTimeout(seconds);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint для проверки здоровья приложения.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Health check endpoint полезен для мониторинга и load balancers.
     * Проверяет доступность внешних сервисов.
     * </p>
     *
     * @return статус здоровья приложения
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("GET /api/demo/health - health check");

        Map<String, Object> health = Map.of(
            "status", "UP",
            "timestamp", System.currentTimeMillis(),
            "application", "spring-openfeign-demo",
            "version", "1.0.0"
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Endpoint для получения информации о конфигурации.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Полезен для отладки - показывает как настроены Feign клиенты.
     * В production такие endpoints должны быть защищены.
     * </p>
     *
     * @return информация о конфигурации приложения
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.debug("GET /api/demo/info - application info");

        Map<String, Object> info = Map.of(
            "application", Map.of(
                "name", "Spring OpenFeign Demo",
                "description", "Educational project demonstrating OpenFeign capabilities",
                "version", "1.0.0"
            ),
            "features", List.of(
                "JSONPlaceholder API integration",
                "HttpBin API testing",
                "Custom error handling",
                "Request/Response logging",
                "Timeout and retry configuration"
            ),
            "endpoints", Map.of(
                "posts", "JSONPlaceholder posts API",
                "users", "JSONPlaceholder users API",
                "httpbin", "HTTP testing utilities"
            )
        );

        return ResponseEntity.ok(info);
    }
}