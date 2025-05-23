package org.gualsh.demo.resttmplt.service;

import org.gualsh.demo.resttmplt.model.Post;
import org.gualsh.demo.resttmplt.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Сервис для демонстрации различных способов использования RestTemplate.
 *
 * <p>Класс показывает все основные паттерны работы с RestTemplate:</p>
 * <ul>
 *   <li>Простые методы (*ForObject, *ForEntity)</li>
 *   <li>Универсальный метод exchange()</li>
 *   <li>Работа с ParameterizedTypeReference для generic типов</li>
 *   <li>Обработка заголовков и статус-кодов</li>
 *   <li>Кэширование результатов</li>
 *   <li>Retry механизмы при сбоях</li>
 * </ul>
 *
 * <p>Особенности реализации:</p>
 * <ul>
 *   <li>@Cacheable - кэширует результаты для уменьшения нагрузки на внешний API</li>
 *   <li>@Retryable - автоматически повторяет запросы при сбоях</li>
 *   <li>@Recover - метод восстановления при исчерпании попыток</li>
 * </ul>
 *
 */
@Service
public class ApiService {

    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private final RestTemplate basicRestTemplate;
    private final RestTemplate advancedRestTemplate;

    @Value("${app.rest-template.external-apis.jsonplaceholder}")
    private String jsonPlaceholderUrl;

    @Value("${app.rest-template.external-apis.httpbin}")
    private String httpBinUrl;

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param basicRestTemplate базовый RestTemplate
     * @param advancedRestTemplate продвинутый RestTemplate с Apache HttpClient
     */
    public ApiService(@Qualifier("basicRestTemplate") RestTemplate basicRestTemplate,
                      @Qualifier("advancedRestTemplate") RestTemplate advancedRestTemplate) {
        this.basicRestTemplate = basicRestTemplate;
        this.advancedRestTemplate = advancedRestTemplate;
    }

    // =============================================================================
    // ДЕМОНСТРАЦИЯ ПРОСТЫХ МЕТОДОВ (*ForObject, *ForEntity)
    // =============================================================================

    /**
     * Получение пользователя по ID с использованием getForObject().
     *
     * <p>Метод getForObject() - самый простой способ получения данных.
     * Возвращает только тело ответа, игнорируя заголовки и статус-код.</p>
     *
     * <p>Подходит для:</p>
     * <ul>
     *   <li>Простых GET запросов</li>
     *   <li>Когда не нужны заголовки ответа</li>
     *   <li>Когда статус-код всегда 200 OK</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return объект пользователя или null при ошибке
     */
    @Cacheable(value = "users", key = "#userId")
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public User getUserById(Long userId) {
        logger.info("Получение пользователя по ID {} с помощью getForObject()", userId);

        try {
            String url = jsonPlaceholderUrl + "/users/{id}";
            User user = basicRestTemplate.getForObject(url, User.class, userId);

            logger.info("Успешно получен пользователь: {}", user);
            return user;

        } catch (RestClientException e) {
            logger.error("Ошибка при получении пользователя по ID {}: {}", userId, e.getMessage());
            throw e; // Перебрасываем для срабатывания @Retryable
        }
    }

    /**
     * Получение пользователя с полной информацией об ответе через getForEntity().
     *
     * <p>Метод getForEntity() возвращает ResponseEntity, который содержит:</p>
     * <ul>
     *   <li>Тело ответа (body)</li>
     *   <li>HTTP статус-код</li>
     *   <li>Заголовки ответа</li>
     * </ul>
     *
     * <p>Используется когда необходимо:</p>
     * <ul>
     *   <li>Проанализировать статус-код</li>
     *   <li>Получить значения заголовков</li>
     *   <li>Обработать различные сценарии ответа</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity с пользователем
     */
    public ResponseEntity<User> getUserWithHeaders(Long userId) {
        logger.info("Получение пользователя с заголовками по ID {}", userId);

        String url = jsonPlaceholderUrl + "/users/{id}";
        ResponseEntity<User> response = basicRestTemplate.getForEntity(url, User.class, userId);

        logger.info("Статус ответа: {}", response.getStatusCode());
        logger.info("Заголовки ответа: {}", response.getHeaders());
        logger.info("Тело ответа: {}", response.getBody());

        return response;
    }

    // =============================================================================
    // ДЕМОНСТРАЦИЯ РАБОТЫ С СПИСКАМИ И ParameterizedTypeReference
    // =============================================================================

    /**
     * Получение списка всех пользователей с использованием ParameterizedTypeReference.
     *
     * <p>ParameterizedTypeReference решает проблему Type Erasure в Java.
     * Без него RestTemplate не может корректно десериализовать List&lt;User&gt;
     * из-за стирания информации о типах во время компиляции.</p>
     *
     * <p>Принцип работы:</p>
     * <ul>
     *   <li>Создается анонимный подкласс, который сохраняет информацию о типе</li>
     *   <li>Jackson получает полную информацию о типе через рефлексию</li>
     *   <li>Корректно происходит десериализация в List&lt;User&gt;</li>
     * </ul>
     *
     * @return список всех пользователей
     */
    @Cacheable("users")
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public List<User> getAllUsers() {
        logger.info("Получение списка всех пользователей");

        try {
            String url = jsonPlaceholderUrl + "/users";

            // ParameterizedTypeReference позволяет корректно десериализовать List<User>
            ParameterizedTypeReference<List<User>> typeReference =
                new ParameterizedTypeReference<List<User>>() {};

            ResponseEntity<List<User>> response = basicRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                typeReference
            );

            List<User> users = response.getBody();
            logger.info("Получено {} пользователей", users != null ? users.size() : 0);

            return users != null ? users : Collections.emptyList();

        } catch (RestClientException e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Получение постов пользователя с кэшированием.
     *
     * @param userId идентификатор пользователя
     * @return список постов пользователя
     */
    @Cacheable(value = "posts", key = "#userId")
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 1.5)
    )
    public List<Post> getUserPosts(Long userId) {
        logger.info("Получение постов пользователя {}", userId);

        try {
            String url = jsonPlaceholderUrl + "/users/{userId}/posts";

            ParameterizedTypeReference<List<Post>> typeReference =
                new ParameterizedTypeReference<List<Post>>() {};

            ResponseEntity<List<Post>> response = advancedRestTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                typeReference,
                userId
            );

            List<Post> posts = response.getBody();
            logger.info("Получено {} постов для пользователя {}",
                posts != null ? posts.size() : 0, userId);

            return posts != null ? posts : Collections.emptyList();

        } catch (RestClientException e) {
            logger.error("Ошибка при получении постов пользователя {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    // =============================================================================
    // ДЕМОНСТРАЦИЯ УНИВЕРСАЛЬНОГО МЕТОДА exchange()
    // =============================================================================

    /**
     * Создание нового поста с использованием exchange() и кастомными заголовками.
     *
     * <p>Метод exchange() - самый универсальный способ работы с HTTP.
     * Позволяет:</p>
     * <ul>
     *   <li>Использовать любой HTTP метод</li>
     *   <li>Настраивать заголовки запроса</li>
     *   <li>Получать полную информацию об ответе</li>
     *   <li>Обрабатывать различные типы контента</li>
     * </ul>
     *
     * @param post данные поста для создания
     * @return созданный пост с присвоенным ID
     */
    public Post createPost(Post post) {
        logger.info("Создание нового поста: {}", post);

        try {
            String url = jsonPlaceholderUrl + "/posts";

            // Настройка заголовков
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "RestTemplate-Demo/1.0");
            headers.set("X-Request-ID", java.util.UUID.randomUUID().toString());

            // Создание HTTP entity с телом и заголовками
            HttpEntity<Post> requestEntity = new HttpEntity<>(post, headers);

            // Выполнение POST запроса
            ResponseEntity<Post> response = advancedRestTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Post.class
            );

            Post createdPost = response.getBody();
            logger.info("Пост успешно создан со статусом {}: {}",
                response.getStatusCode(), createdPost);

            return createdPost;

        } catch (RestClientException e) {
            logger.error("Ошибка при создании поста: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Обновление поста с использованием PUT запроса.
     *
     * @param postId идентификатор поста
     * @param post обновленные данные поста
     * @return обновленный пост
     */
    public Post updatePost(Long postId, Post post) {
        logger.info("Обновление поста с ID {}: {}", postId, post);

        try {
            String url = jsonPlaceholderUrl + "/posts/{id}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Post> requestEntity = new HttpEntity<>(post, headers);

            ResponseEntity<Post> response = basicRestTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                Post.class,
                postId
            );

            Post updatedPost = response.getBody();
            logger.info("Пост успешно обновлен: {}", updatedPost);

            return updatedPost;

        } catch (RestClientException e) {
            logger.error("Ошибка при обновлении поста {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    /**
     * Удаление поста с проверкой статус-кода.
     *
     * @param postId идентификатор поста для удаления
     * @return true если пост успешно удален
     */
    public boolean deletePost(Long postId) {
        logger.info("Удаление поста с ID {}", postId);

        try {
            String url = jsonPlaceholderUrl + "/posts/{id}";

            ResponseEntity<Void> response = basicRestTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void.class,
                postId
            );

            boolean isDeleted = response.getStatusCode().is2xxSuccessful();
            logger.info("Статус удаления поста {}: {} ({})",
                postId, isDeleted, response.getStatusCode());

            return isDeleted;

        } catch (RestClientException e) {
            logger.error("Ошибка при удалении поста {}: {}", postId, e.getMessage());
            return false;
        }
    }

    // =============================================================================
    // ДЕМОНСТРАЦИЯ РАБОТЫ С ЗАГОЛОВКАМИ И РАЗЛИЧНЫМИ ТИПАМИ КОНТЕНТА
    // =============================================================================

    /**
     * Демонстрация работы с различными заголовками и типами контента.
     *
     * <p>Показывает как:</p>
     * <ul>
     *   <li>Отправлять кастомные заголовки</li>
     *   <li>Получать заголовки ответа</li>
     *   <li>Работать с различными Content-Type</li>
     *   <li>Обрабатывать ответы в виде Map для гибкости</li>
     * </ul>
     *
     * @return информация о запросе от httpbin.org
     */
    public Map<String, Object> demonstrateHeaders() {
        logger.info("Демонстрация работы с заголовками");

        try {
            String url = httpBinUrl + "/headers";

            // Настройка множественных заголовков
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Custom-Header", "Demo-Value");
            headers.set("X-Client-Version", "1.0.0");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "RestTemplate-Demo");

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            // Использование ParameterizedTypeReference для Map
            ParameterizedTypeReference<Map<String, Object>> typeReference =
                new ParameterizedTypeReference<Map<String, Object>>() {};

            ResponseEntity<Map<String, Object>> response = advancedRestTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                typeReference
            );

            logger.info("Получены заголовки: {}", response.getHeaders());
            return response.getBody();

        } catch (RestClientException e) {
            logger.error("Ошибка при демонстрации заголовков: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // =============================================================================
    // МЕТОДЫ ВОССТАНОВЛЕНИЯ ДЛЯ @Recover
    // =============================================================================

    /**
     * Метод восстановления для getUserById при исчерпании попыток.
     *
     * <p>Аннотация @Recover срабатывает когда все попытки @Retryable исчерпаны.
     * Позволяет вернуть резервное значение или выполнить альтернативную логику.</p>
     *
     * @param ex исключение, которое привело к сбою
     * @param userId идентификатор пользователя
     * @return резервный объект пользователя
     */
    @Recover
    public User recoverGetUserById(RestClientException ex, Long userId) {
        logger.warn("Восстановление после сбоя получения пользователя {}: {}", userId, ex.getMessage());

        // Возвращаем резервного пользователя
        User fallbackUser = new User();
        fallbackUser.setId(userId);
        fallbackUser.setName("Unknown User");
        fallbackUser.setUsername("unknown");
        fallbackUser.setEmail("unknown@example.com");

        return fallbackUser;
    }

    /**
     * Метод восстановления для getAllUsers.
     *
     * @param ex исключение
     * @return пустой список как резервное значение
     */
    @Recover
    public List<User> recoverGetAllUsers(RestClientException ex) {
        logger.warn("Восстановление после сбоя получения списка пользователей: {}", ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Метод восстановления для getUserPosts.
     *
     * @param ex исключение
     * @param userId идентификатор пользователя
     * @return пустой список постов
     */
    @Recover
    public List<Post> recoverGetUserPosts(RestClientException ex, Long userId) {
        logger.warn("Восстановление после сбоя получения постов пользователя {}: {}", userId, ex.getMessage());
        return Collections.emptyList();
    }
}