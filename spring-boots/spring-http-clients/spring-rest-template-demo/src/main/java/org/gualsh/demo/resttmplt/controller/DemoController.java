package org.gualsh.demo.resttmplt.controller;

import org.gualsh.demo.resttmplt.model.Post;
import org.gualsh.demo.resttmplt.model.User;
import org.gualsh.demo.resttmplt.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST контроллер для демонстрации возможностей RestTemplate.
 *
 * <p>Предоставляет HTTP endpoints для тестирования различных
 * функций RestTemplate через веб-интерфейс или REST клиенты.</p>
 *
 * <p>Доступные операции:</p>
 * <ul>
 *   <li>GET /api/users - получение всех пользователей</li>
 *   <li>GET /api/users/{id} - получение пользователя по ID</li>
 *   <li>GET /api/users/{id}/posts - получение постов пользователя</li>
 *   <li>POST /api/posts - создание нового поста</li>
 *   <li>PUT /api/posts/{id} - обновление поста</li>
 *   <li>DELETE /api/posts/{id} - удаление поста</li>
 *   <li>GET /api/demo/headers - демонстрация работы с заголовками</li>
 * </ul>
 *
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    private final ApiService apiService;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param apiService сервис для работы с внешним API
     */
    public DemoController(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Получение всех пользователей.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>Использование ParameterizedTypeReference для List&lt;User&gt;</li>
     *   <li>Кэширование результатов</li>
     *   <li>Retry механизм при сбоях</li>
     * </ul>
     *
     * @return список всех пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("REST: Запрос всех пользователей");

        List<User> users = apiService.getAllUsers();

        logger.info("REST: Возвращено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Получение пользователя по ID.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>Простой метод getForObject()</li>
     *   <li>Обработку path параметров</li>
     *   <li>Кэширование по ключу</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return пользователь или 404 если не найден
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
        logger.info("REST: Запрос пользователя с ID {}", userId);

        User user = apiService.getUserById(userId);

        if (user != null) {
            logger.info("REST: Найден пользователь {}", user.getUsername());
            return ResponseEntity.ok(user);
        } else {
            logger.warn("REST: Пользователь с ID {} не найден", userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получение пользователя с полной информацией об HTTP ответе.
     *
     * <p>Демонстрирует использование getForEntity() для получения
     * заголовков и статус-кода ответа.</p>
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity с пользователем и метаданными
     */
    @GetMapping("/users/{id}/details")
    public ResponseEntity<User> getUserWithDetails(@PathVariable("id") Long userId) {
        logger.info("REST: Запрос детальной информации о пользователе {}", userId);

        ResponseEntity<User> response = apiService.getUserWithHeaders(userId);

        logger.info("REST: Получен ответ со статусом {} и {} заголовками",
            response.getStatusCode(), response.getHeaders().size());

        return response;
    }

    /**
     * Получение постов пользователя.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>Работу с path параметрами в URL</li>
     *   <li>Десериализацию списка объектов</li>
     *   <li>Кэширование по составному ключу</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return список постов пользователя
     */
    @GetMapping("/users/{id}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable("id") Long userId) {
        logger.info("REST: Запрос постов пользователя {}", userId);

        List<Post> posts = apiService.getUserPosts(userId);

        logger.info("REST: Найдено {} постов для пользователя {}", posts.size(), userId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Создание нового поста.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>POST запрос с JSON телом</li>
     *   <li>Настройку кастомных заголовков</li>
     *   <li>Использование exchange() для полного контроля</li>
     * </ul>
     *
     * @param post данные нового поста
     * @return созданный пост с присвоенным ID
     */
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        logger.info("REST: Создание нового поста: {}", post.getTitle());

        Post createdPost = apiService.createPost(post);

        logger.info("REST: Пост создан с ID {}", createdPost.getId());
        return ResponseEntity.ok(createdPost);
    }

    /**
     * Обновление существующего поста.
     *
     * <p>Демонстрирует PUT запрос для полного обновления ресурса.</p>
     *
     * @param postId идентификатор поста
     * @param post новые данные поста
     * @return обновленный пост
     */
    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable("id") Long postId,
                                           @RequestBody Post post) {
        logger.info("REST: Обновление поста с ID {}", postId);

        Post updatedPost = apiService.updatePost(postId, post);

        logger.info("REST: Пост {} успешно обновлен", postId);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * Удаление поста.
     *
     * <p>Демонстрирует DELETE запрос и обработку различных статус-кодов.</p>
     *
     * @param postId идентификатор поста для удаления
     * @return статус операции удаления
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable("id") Long postId) {
        logger.info("REST: Удаление поста с ID {}", postId);

        boolean deleted = apiService.deletePost(postId);

        Map<String, Object> response = Map.of(
            "postId", postId,
            "deleted", deleted,
            "message", deleted ? "Пост успешно удален" : "Ошибка при удалении поста"
        );

        logger.info("REST: Результат удаления поста {}: {}", postId, deleted);

        return deleted ?
            ResponseEntity.ok(response) :
            ResponseEntity.internalServerError().body(response);
    }

    /**
     * Демонстрация работы с заголовками.
     *
     * <p>Показывает как RestTemplate может:</p>
     * <ul>
     *   <li>Отправлять кастомные заголовки</li>
     *   <li>Получать заголовки ответа</li>
     *   <li>Работать с различными типами контента</li>
     * </ul>
     *
     * @return информация о заголовках запроса
     */
    @GetMapping("/demo/headers")
    public ResponseEntity<Map<String, Object>> demonstrateHeaders() {
        logger.info("REST: Демонстрация работы с заголовками");

        Map<String, Object> headerInfo = apiService.demonstrateHeaders();

        logger.info("REST: Получена информация о заголовках");
        return ResponseEntity.ok(headerInfo);
    }

    /**
     * Получение информации о кэше.
     *
     * <p>Вспомогательный endpoint для мониторинга состояния кэша.</p>
     *
     * @return информация о кэшированных данных
     */
    @GetMapping("/cache/info")
    public ResponseEntity<Map<String, String>> getCacheInfo() {
        logger.info("REST: Запрос информации о кэше");

        Map<String, String> cacheInfo = Map.of(
            "users_cache", "Кэширует пользователей по ID и полный список",
            "posts_cache", "Кэширует посты пользователей",
            "ttl", "По умолчанию не ограничено (Simple cache)",
            "eviction", "LRU при достижении лимита памяти"
        );

        return ResponseEntity.ok(cacheInfo);
    }

    /**
     * Очистка кэша (для тестирования).
     *
     * <p>Позволяет очистить кэш для проверки актуального состояния данных.</p>
     *
     * @return статус операции очистки
     */
    @DeleteMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        logger.info("REST: Запрос на очистку кэша");

        // В реальном проекте здесь был бы код очистки кэша
        // Например: cacheManager.getCache("users").clear();

        Map<String, String> response = Map.of(
            "status", "success",
            "message", "Кэш очищен (симуляция)"
        );

        return ResponseEntity.ok(response);
    }
}