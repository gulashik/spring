package org.gualsh.demo.webclient.service;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.client.JsonPlaceholderClient;
import org.gualsh.demo.webclient.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Обновленный сервис для работы с JSONPlaceholder API через @HttpExchange клиент.
 *
 * <p>Основные изменения по сравнению с версией на WebClient:</p>
 * <ul>
 *   <li>Использует декларативный JsonPlaceholderClient вместо прямых вызовов WebClient</li>
 *   <li>Упрощенная логика - большая часть HTTP деталей скрыта в клиенте</li>
 *   <li>Фокус на бизнес-логике вместо HTTP конфигурации</li>
 *   <li>Сохранена вся функциональность: кэширование, retry, композиция запросов</li>
 * </ul>
 *
 * <p>Преимущества @HttpExchange подхода:</p>
 * <ul>
 *   <li>Декларативность - меньше boilerplate кода</li>
 *   <li>Легкость тестирования - можно мокировать интерфейс клиента</li>
 *   <li>Централизованная настройка HTTP в конфигурации</li>
 *   <li>Автоматическая обработка сериализации/десериализации</li>
 *   <li>Консистентная обработка ошибок</li>
 * </ul>
 *
 * @see JsonPlaceholderClient
 */
@Slf4j
@Service
public class JsonPlaceholderService {

    private final JsonPlaceholderClient client;
    private final int maxAttempts;
    private final long delay;

    /**
     * Конструктор с внедрением HttpExchange клиента.
     *
     * <p>Обратите внимание на изменение в зависимостях:
     * вместо WebClient теперь внедряется типизированный клиент.</p>
     *
     * @param client HttpExchange клиент для JSONPlaceholder API
     * @param maxAttempts максимальное количество попыток retry
     * @param delay задержка между попытками в миллисекундах
     */
    public JsonPlaceholderService(
        @Qualifier("jsonPlaceholderClient") JsonPlaceholderClient client,
        @Value("${external-api.jsonplaceholder.max-attempts:3}") int maxAttempts,
        @Value("${external-api.jsonplaceholder.delay:1000}") long delay
    ) {
        this.client = client;
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        log.info("JsonPlaceholderService initialized with HttpExchange client, maxAttempts: {}, delay: {}ms",
            maxAttempts, delay);
    }

    /**
     * Получает список всех пользователей с кэшированием.
     *
     * <p>Сравнение с WebClient версией:</p>
     * <pre>{@code
     * // Старая версия (WebClient):
     * return webClient.get().uri("/users").retrieve()
     *     .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {})
     *     .retryWhen(...)
     *     .doOnSuccess(...)
     *
     * // Новая версия (HttpExchange):
     * return client.getAllUsers()
     *     .doOnSuccess(...)
     * }</pre>
     *
     * @return Mono со списком пользователей
     */
    @Cacheable(value = "users", key = "'all-users'")
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public Mono<List<UserDto>> getAllUsers() {
        log.debug("Fetching all users from JSONPlaceholder API via HttpExchange");

        return client.getAllUsers()
            .doOnSuccess(users -> log.info("Successfully fetched {} users", users.size()))
            .doOnError(error -> log.error("Error fetching users: {}", error.getMessage()));
    }

    /**
     * Получает пользователя по ID с кэшированием и retry.
     *
     * <p>HttpExchange автоматически обрабатывает подстановку пути и
     * сериализацию ответа. Нет необходимости в explicit handling 404.</p>
     *
     * @param userId идентификатор пользователя
     * @return Mono с пользователем
     */
    @Cacheable(value = "users", key = "#userId")
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public Mono<UserDto> getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);

        return client.getUserById(userId)
            .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getUsername()))
            .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()));
    }

    /**
     * Получает посты пользователя.
     *
     * <p>HttpExchange автоматически добавляет query параметры в URL.</p>
     *
     * @param userId идентификатор пользователя
     * @return Flux с постами пользователя
     */
    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500)
    )
    public Flux<PostDto> getPostsByUserId(Long userId) {
        log.debug("Fetching posts for user ID: {}", userId);

        return client.getPostsByUserId(userId)
            .doOnComplete(() -> log.info("Successfully fetched posts for user {}", userId))
            .doOnError(error -> log.error("Error fetching posts for user {}: {}", userId, error.getMessage()));
    }

    /**
     * Получает все посты с пагинацией.
     *
     * <p>Бизнес-логика пагинации остается в сервисе,
     * а HTTP детали скрыты в клиенте.</p>
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return Mono с пагинированным ответом
     */
    public Mono<PagedResponseDto<PostDto>> getAllPostsPaginated(int page, int size) {
        log.debug("Fetching posts with pagination: page={}, size={}", page, size);

        int start = page * size;

        return client.getAllPostsPaginated(start, size)
            .map(posts -> PagedResponseDto.<PostDto>builder()
                .data(posts)
                .page(page)
                .size(size)
                .total((long) posts.size())
                .totalPages((posts.size() + size - 1) / size)
                .hasNext(posts.size() == size)
                .hasPrevious(page > 0)
                .timestamp(LocalDateTime.now())
                .build())
            .doOnSuccess(response -> log.info("Successfully fetched {} posts on page {}",
                response.getData().size(), page));
    }

    /**
     * Создает новый пост.
     *
     * <p>HttpExchange автоматически сериализует DTO в JSON
     * и десериализует ответ.</p>
     *
     * @param createPostDto данные для создания поста
     * @return Mono с созданным постом
     */
    public Mono<PostDto> createPost(CreatePostDto createPostDto) {
        log.debug("Creating new post for user: {}", createPostDto.getUserId());

        return client.createPost(createPostDto)
            .doOnSuccess(post -> log.info("Successfully created post with ID: {}", post.getId()))
            .doOnError(error -> log.error("Error creating post: {}", error.getMessage()));
    }

    /**
     * Обновляет существующий пост полностью.
     *
     * <p>HttpExchange упрощает PUT запросы - не нужно явно указывать
     * content type и body serialization.</p>
     *
     * @param postId идентификатор поста для обновления
     * @param updatePostDto новые данные поста
     * @return Mono с обновленным постом
     */
    public Mono<PostDto> updatePost(Long postId, UpdatePostDto updatePostDto) {
        log.debug("Updating post with ID: {}", postId);

        return client.updatePost(postId, updatePostDto)
            .doOnSuccess(post -> log.info("Successfully updated post: {}", post.getId()))
            .doOnError(error -> log.error("Error updating post {}: {}", postId, error.getMessage()));
    }

    /**
     * Частично обновляет пост.
     *
     * <p>PATCH операции с Map параметрами работают без дополнительной настройки.</p>
     *
     * @param postId идентификатор поста
     * @param updates Map с полями для обновления
     * @return Mono с обновленным постом
     */
    public Mono<PostDto> patchPost(Long postId, Map<String, Object> updates) {
        log.debug("Partially updating post with ID: {}", postId);

        return client.patchPost(postId, updates)
            .doOnSuccess(post -> log.info("Successfully patched post: {}", post.getId()))
            .doOnError(error -> log.error("Error patching post {}: {}", postId, error.getMessage()));
    }

    /**
     * Удаляет пост по ID.
     *
     * <p>DELETE операции с Mono&lt;Void&gt; возвращаемым типом.</p>
     *
     * @param postId идентификатор поста для удаления
     * @return Mono<Void> подтверждающий удаление
     */
    public Mono<Void> deletePost(Long postId) {
        log.debug("Deleting post with ID: {}", postId);

        return client.deletePost(postId)
            .doOnSuccess(unused -> log.info("Successfully deleted post: {}", postId))
            .doOnError(error -> log.error("Error deleting post {}: {}", postId, error.getMessage()));
    }

    /**
     * Получает комментарии к посту.
     *
     * <p>Демонстрирует работу с вложенными URL путями.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    public Flux<CommentDto> getCommentsByPostId(Long postId) {
        log.debug("Fetching comments for post ID: {}", postId);

        return client.getCommentsByPostId(postId)
            .doOnComplete(() -> log.info("Successfully fetched comments for post {}", postId))
            .doOnError(error -> log.error("Error fetching comments for post {}: {}", postId, error.getMessage()));
    }

    /**
     * Получает пост по ID.
     *
     * <p>Простая операция получения конкретного поста.</p>
     *
     * @param postId идентификатор поста
     * @return Mono с постом
     */
    public Mono<PostDto> getPostById(Long postId) {
        log.debug("Fetching post by ID: {}", postId);

        return client.getPostById(postId)
            .doOnSuccess(post -> log.info("Successfully fetched post: {}", post.getTitle()))
            .doOnError(error -> log.error("Error fetching post {}: {}", postId, error.getMessage()));
    }

    /**
     * Метод восстановления для getUserById при исчерпании retry попыток.
     *
     * <p>@Recover работает одинаково с HttpExchange клиентами.</p>
     *
     * @param ex исключение, вызвавшее необходимость восстановления
     * @param userId идентификатор пользователя из оригинального вызова
     * @return Mono с пользователем по умолчанию
     */
    @Recover
    public Mono<UserDto> recoverGetUserById(Exception ex, Long userId) {
        log.warn("Recovering from getUserById failure for user {}: {}", userId, ex.getMessage());

        return Mono.just(UserDto.builder()
            .id(userId)
            .username("unknown")
            .name("Unknown User")
            .email("unknown@example.com")
            .build());
    }

    /**
     * Композиция нескольких запросов для получения пользователя с постами.
     *
     * <p>Бизнес-логика композиции остается неизменной,
     * упрощаются только базовые HTTP операции.</p>
     *
     * @param userId идентификатор пользователя
     * @return Mono с объединенными данными пользователя и его постов
     */
    public Mono<Map<String, Object>> getUserWithPosts(Long userId) {
        log.debug("Fetching user with posts for ID: {}", userId);

        Mono<UserDto> userMono = getUserById(userId);
        Mono<List<PostDto>> postsMono = getPostsByUserId(userId).collectList();

        return Mono.zip(userMono, postsMono)
            .map(tuple -> Map.of(
                "user", tuple.getT1(),
                "posts", tuple.getT2(),
                "postsCount", tuple.getT2().size(),
                "timestamp", LocalDateTime.now()
            ))
            .doOnSuccess(result -> log.info("Successfully fetched user with {} posts",
                ((List<?>) result.get("posts")).size()));
    }

    /**
     * Batch операция для получения нескольких пользователей.
     *
     * <p>Параллельная обработка нескольких запросов остается такой же.</p>
     *
     * @param userIds список идентификаторов пользователей
     * @return Flux с пользователями
     */
    public Flux<UserDto> getUsersBatch(List<Long> userIds) {
        log.debug("Fetching batch of {} users", userIds.size());

        return Flux.fromIterable(userIds)
            .flatMap(this::getUserById, 5) // Параллельность = 5
            .doOnComplete(() -> log.info("Completed batch fetch for {} users", userIds.size()));
    }

    /**
     * Альтернативный способ получения комментариев через query параметр.
     *
     * <p>Демонстрирует гибкость HttpExchange в работе с различными
     * способами запроса одних и тех же данных.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    public Flux<CommentDto> getCommentsByPostIdAlternative(Long postId) {
        log.debug("Fetching comments for post ID: {} (alternative method)", postId);

        return client.getCommentsByPostIdQuery(postId)
            .doOnComplete(() -> log.info("Successfully fetched comments for post {} (alternative)", postId))
            .doOnError(error -> log.error("Error fetching comments for post {} (alternative): {}",
                postId, error.getMessage()));
    }

    /**
     * Получает все посты без фильтрации.
     *
     * <p>Базовая операция для получения всех постов.</p>
     *
     * @return Mono со списком всех постов
     */
    public Mono<List<PostDto>> getAllPosts() {
        log.debug("Fetching all posts");

        return client.getAllPosts()
            .doOnSuccess(posts -> log.info("Successfully fetched {} posts", posts.size()))
            .doOnError(error -> log.error("Error fetching all posts: {}", error.getMessage()));
    }
}