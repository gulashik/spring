package org.gualsh.demo.openfeign.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.openfeign.client.HttpBinClient;
import org.gualsh.demo.openfeign.client.JsonPlaceholderClient;
import org.gualsh.demo.openfeign.dto.request.CreatePostRequest;
import org.gualsh.demo.openfeign.dto.response.HttpBinResponse;
import org.gualsh.demo.openfeign.dto.response.Post;
import org.gualsh.demo.openfeign.dto.response.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для демонстрации возможностей OpenFeign клиентов.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * Этот сервис демонстрирует best practices использования OpenFeign клиентов
 * в бизнес-логике приложения. Показывает как правильно обрабатывать ошибки,
 * логировать запросы и структурировать код для работы с внешними API.
 * </p>
 *
 * <h3>Архитектурные принципы:</h3>
 * <ul>
 *   <li><strong>Dependency Injection</strong> - инжектирование Feign клиентов через конструктор</li>
 *   <li><strong>Error Handling</strong> - централизованная обработка ошибок</li>
 *   <li><strong>Logging</strong> - подробное логирование для отладки</li>
 *   <li><strong>Business Logic</strong> - инкапсуляция бизнес-правил</li>
 * </ul>
 *
 * <h3>Best Practices:</h3>
 * <ul>
 *   <li>Не используйте Feign клиенты напрямую в контроллерах</li>
 *   <li>Группируйте связанные операции в одном сервисе</li>
 *   <li>Логируйте важные операции для мониторинга</li>
 *   <li>Обрабатывайте ошибки на уровне сервиса</li>
 * </ul>
 *
 * @author Generated for educational purposes
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    private final JsonPlaceholderClient jsonPlaceholderClient;
    private final HttpBinClient httpBinClient;

    /**
     * Получает все посты с дополнительной бизнес-логикой.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует как добавлять value поверх простых API вызовов:
     * логирование, фильтрация, обогащение данных.
     * </p>
     *
     * @return список всех постов
     */
    public List<Post> getAllPosts() {
        log.info("Fetching all posts from JSONPlaceholder API");

        try {
            List<Post> posts = jsonPlaceholderClient.getAllPosts();
            log.info("Successfully fetched {} posts", posts.size());
            return posts;
        } catch (Exception e) {
            log.error("Failed to fetch posts", e);
            throw new RuntimeException("Unable to fetch posts from external API", e);
        }
    }

    /**
     * Получает посты с пагинацией для оптимальной работы с большими объемами данных.
     *
     * @param start начальная позиция для выборки (offset)
     * @param limit максимальное количество записей для возврата
     * @return список постов в заданном диапазоне
     * @throws RuntimeException если произошла ошибка при получении данных
     */
    public List<Post> getPostsWithPagination(Integer start, Integer limit) {
        try {
            log.info("Получение постов с пагинацией: start={}, limit={}", start, limit);
            return jsonPlaceholderClient.getPostsWithPagination(start, limit);
        } catch (Exception e) {
            log.error("Ошибка при получении постов с пагинацией: {}", e.getMessage());
            throw new RuntimeException("Не удалось получить посты с пагинацией", e);
        }
    }

    /**
     * Получает пост по ID с валидацией.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает как добавлять валидацию и обработку ошибок
     * поверх Feign клиента.
     * </p>
     *
     * @param id идентификатор поста
     * @return Optional с постом или empty если не найден
     */
    public Optional<Post> getPostById(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid post ID provided: {}", id);
            return Optional.empty();
        }

        log.info("Fetching post with ID: {}", id);

        try {
            Post post = jsonPlaceholderClient.getPostById(id);
            log.info("Successfully fetched post: {}", post.getShortDescription());
            return Optional.of(post);
        } catch (Exception e) {
            log.warn("Post with ID {} not found or error occurred", id, e);
            return Optional.empty();
        }
    }

    /**
     * Создает новый пост с валидацией и логированием.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует pre и post обработку при создании ресурсов
     * через внешние API.
     * </p>
     *
     * @param request запрос на создание поста
     * @return созданный пост
     */
    public Post createPost(CreatePostRequest request) {
        log.info("Creating new post: {}", request.toLogString());

        // Валидация на уровне сервиса
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid post data provided");
        }

        try {
            // Нормализация данных перед отправкой
            CreatePostRequest normalizedRequest = request.normalize();

            Post createdPost = jsonPlaceholderClient.createPost(normalizedRequest);
            log.info("Successfully created post with ID: {}", createdPost.getId());

            return createdPost;
        } catch (Exception e) {
            log.error("Failed to create post", e);
            throw new RuntimeException("Unable to create post via external API", e);
        }
    }

    /**
     * Получает посты пользователя с проверкой существования пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает композицию нескольких API вызовов для реализации
     * более сложной бизнес-логики.
     * </p>
     *
     * @param userId идентификатор пользователя
     * @return список постов пользователя
     */
    public List<Post> getUserPosts(Long userId) {
        log.info("Fetching posts for user ID: {}", userId);

        // Сначала проверяем, существует ли пользователь
        Optional<User> user = getUserById(userId);
        if (user.isEmpty()) {
            log.warn("User with ID {} not found", userId);
            throw new IllegalArgumentException("User not found");
        }

        try {
            List<Post> userPosts = jsonPlaceholderClient.getPostsByUserId(userId);
            log.info("Found {} posts for user {} ({})",
                userPosts.size(), userId, user.get().getDisplayName());
            return userPosts;
        } catch (Exception e) {
            log.error("Failed to fetch posts for user {}", userId, e);
            throw new RuntimeException("Unable to fetch user posts", e);
        }
    }

    /**
     * Получает пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return Optional с пользователем
     */
    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        try {
            User user = jsonPlaceholderClient.getUserById(id);
            return Optional.of(user);
        } catch (Exception e) {
            log.warn("User with ID {} not found", id);
            return Optional.empty();
        }
    }

    /**
     * Демонстрирует различные возможности HttpBin API.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Этот метод показывает как тестировать различные аспекты
     * HTTP коммуникации используя HttpBin.
     * </p>
     *
     * @return результаты различных тестов
     */
    public Map<String, Object> demonstrateHttpBinFeatures() {
        log.info("Demonstrating HttpBin API features");

        Map<String, Object> results = new HashMap<>();

        try {
            // Тест GET запроса с параметрами
            HttpBinResponse getResponse = httpBinClient.testGet("value1", "value2");
            results.put("get_test", getResponse.getSummary());

            // Тест POST с JSON
            Map<String, Object> jsonData = Map.of(
                "message", "Hello from OpenFeign",
                "timestamp", System.currentTimeMillis(),
                "nested", Map.of("key", "value")
            );
            HttpBinResponse postResponse = httpBinClient.testPostJson(jsonData);
            results.put("post_json_test", postResponse.getSummary());

            // Тест с custom заголовками
            HttpBinResponse putResponse = httpBinClient.testPutWithHeaders(
                "demo-api-key-123",
                "req-" + System.currentTimeMillis(),
                Map.of("action", "update", "version", "1.0")
            );
            results.put("custom_headers_test", putResponse.getSummary());

            // Получение IP адреса
            HttpBinResponse ipResponse = httpBinClient.getClientIP();
            results.put("client_ip", ipResponse.getOrigin());

            log.info("Successfully completed HttpBin demonstrations");

        } catch (Exception e) {
            log.error("Error during HttpBin demonstration", e);
            results.put("error", "Failed to complete some tests: " + e.getMessage());
        }

        return results;
    }

    /**
     * Тестирует обработку различных HTTP статус кодов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует как ваш ErrorDecoder обрабатывает различные
     * типы HTTP ошибок.
     * </p>
     *
     * @param statusCode статус код для тестирования
     * @return результат теста или информация об ошибке
     */
    public Map<String, Object> testErrorHandling(int statusCode) {
        log.info("Testing error handling for status code: {}", statusCode);

        Map<String, Object> result = new HashMap<>();
        result.put("requested_status", statusCode);

        try {
            HttpBinResponse response = httpBinClient.testStatusCode(statusCode);
            result.put("success", true);
            result.put("response", response.getSummary());
            log.info("Status code {} handled successfully", statusCode);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error_type", e.getClass().getSimpleName());
            result.put("error_message", e.getMessage());
            log.info("Status code {} triggered error: {}", statusCode, e.getMessage());
        }

        return result;
    }

    /**
     * Тестирует таймауты и задержки.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает как приложение ведет себя при медленных ответах
     * от внешних сервисов.
     * </p>
     *
     * @param delaySeconds количество секунд задержки
     * @return результат теста таймаута
     */
    public Map<String, Object> testTimeout(int delaySeconds) {
        log.info("Testing timeout with {} seconds delay", delaySeconds);

        Map<String, Object> result = new HashMap<>();
        result.put("delay_seconds", delaySeconds);

        long startTime = System.currentTimeMillis();

        try {
            HttpBinResponse response = httpBinClient.testDelay(delaySeconds);
            long duration = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("actual_duration_ms", duration);
            result.put("response", response.getSummary());

            log.info("Delay test completed in {} ms", duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            result.put("success", false);
            result.put("failed_after_ms", duration);
            result.put("error_type", e.getClass().getSimpleName());
            result.put("error_message", e.getMessage());

            log.warn("Delay test failed after {} ms: {}", duration, e.getMessage());
        }

        return result;
    }
}