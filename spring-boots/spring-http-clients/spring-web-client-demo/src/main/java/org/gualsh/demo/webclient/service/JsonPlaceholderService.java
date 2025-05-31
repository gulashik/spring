package org.gualsh.demo.webclient.service;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с JSONPlaceholder API.
 *
 * <p>Предоставляет методы для работы с пользователями, постами и комментариями.
 * Включает кэширование, retry механизм и reactive подход.</p>
 *
 * <p>Особенности реализации:</p>
 * <ul>
 *   <li>Использование {@link ParameterizedTypeReference} для generic типов</li>
 *   <li>Кэширование популярных запросов с помощью Spring Cache</li>
 *   <li>Retry механизм для обработки временных сбоев</li>
 *   <li>Reactive streams для асинхронной обработки</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
 * @see WebClient
 * @see ParameterizedTypeReference
 */
@Slf4j
@Service
public class JsonPlaceholderService {

    private final WebClient jsonPlaceholderWebClient;
    private final int maxAttempts;
    private final long delay;

    /**
     * Конструктор сервиса с внедрением зависимостей.
     *
     * @param jsonPlaceholderWebClient WebClient для JSONPlaceholder API
     * @param maxAttempts              максимальное количество попыток retry
     * @param delay                    задержка между попытками в миллисекундах
     */
    public JsonPlaceholderService(
        @Qualifier("jsonPlaceholderWebClient") WebClient jsonPlaceholderWebClient,
        @Value("${external-api.jsonplaceholder.max-attempts:3}") int maxAttempts,
        @Value("${external-api.jsonplaceholder.delay:1000}") long delay
    ) {
        this.jsonPlaceholderWebClient = jsonPlaceholderWebClient;
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        log.info("JsonPlaceholderService initialized with maxAttempts: {}, delay: {}ms", maxAttempts, delay);
    }

    /**
     * Получает список всех пользователей с кэшированием.
     *
     * <p>Демонстрирует использование:</p>
     * <ul>
     *   <li>{@link ParameterizedTypeReference} для List&lt;UserDto&gt;</li>
     *   <li>Spring Cache для кэширования результата</li>
     *   <li>Retry механизм на уровне WebClient</li>
     * </ul>
     *
     * @return Mono со списком пользователей
     */
    @Cacheable(value = "users", key = "'all-users'")
    public Mono<List<UserDto>> getAllUsers() {
        log.debug("Fetching all users from JSONPlaceholder API");

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .get()
            .uri("/users")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(
                new ParameterizedTypeReference<List<UserDto>>() {}
            )
            /*
             * Реализация механизма повторных попыток (retry) непосредственно в цепочке реактивных операций.
             *
             * Особенности:
             * - Использует экспоненциальную задержку с увеличением времени между попытками
             * - Повторяет запрос только при ошибках сервера (5xx)
             * - Логирует исчерпание попыток с детальной информацией
             * - В случае исчерпания всех попыток генерирует информативное исключение
             *
             * Альтернативой этому подходу является аннотация @Retryable, используемая в других методах.
             */
            .retryWhen(
                Retry.backoff(maxAttempts, Duration.ofMillis(delay))
                    .filter(throwable -> throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().is5xxServerError())
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Retry exhausted for getAllUsers after {} attempts", retrySignal.totalRetries());
                            return new RuntimeException("Failed to fetch users after " + retrySignal.totalRetries() + " attempts");
                        }
                    )
            )
            .doOnSuccess(users -> log.info("Successfully fetched {} users", users.size()))
            .doOnError(error -> log.error("Error fetching users: {}", error.getMessage()));
    }

    /**
     * Получает пользователя по ID с кэшированием и retry.
     *
     * @param userId идентификатор пользователя
     * @return Mono с пользователем или ошибкой если не найден
     */
    @Cacheable(value = "users", key = "#userId")
    public Mono<UserDto> getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .get()
            .uri("/users/{id}", userId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            /*
             * Действия при статусе
             * Можно кинуть RuntimeException при не нужном статусе.
             * НО! У нас настроен ExchangeFilterFunction errorHandlingFilter()
             * Важно понимать порядок выполнения:
             * 1. **Первым** сработает обработчик в методе `getUserById`, который создаст `onStatus``RuntimeException`
             * 2. Глобальный `errorHandlingFilter()` **не будет выполнен** для этого запроса, поскольку ошибка уже создана и цепочка обработки ответа прервана
             */
            .onStatus(
                HttpStatus.NOT_FOUND::equals,
                response -> Mono.error(new RuntimeException("User not found with ID: " + userId))
            )
            .bodyToMono(UserDto.class)
            /*
             * Реализация механизма повторных попыток (retry) непосредственно в цепочке реактивных операций.
             *
             * Особенности:
             * - Использует экспоненциальную задержку с увеличением времени между попытками
             * - Повторяет запрос только при ошибках сервера (5xx)
             * - Логирует исчерпание попыток с детальной информацией
             * - В случае исчерпания всех попыток генерирует информативное исключение
             *
             * Альтернативой этому подходу является аннотация @Retryable, используемая в других методах.
             */
            .retryWhen(
                Retry.backoff(maxAttempts, Duration.ofMillis(delay))
                    .filter(throwable -> {
                        if (throwable instanceof WebClientResponseException ex) {
                            return ex.getStatusCode().is5xxServerError();
                        }
                        return false;
                    })
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            log.error("Retry exhausted for getUserById after {} attempts", retrySignal.totalRetries());
                            return new RuntimeException("Failed to fetch user after " + retrySignal.totalRetries() + " attempts");
                        }
                    )
            )
            .doOnSuccess(user -> log.info("Successfully fetched user: {}", user.getUsername()))
            .doOnError(error -> log.error("Error fetching user {}: {}", userId, error.getMessage()));
    }

    /**
     * Получает посты пользователя с использованием query параметров.
     *
     * @param userId идентификатор пользователя
     * @return Flux с постами пользователя
     */
    public Flux<PostDto> getPostsByUserId(Long userId) {
        log.debug("Fetching posts for user ID: {}", userId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/posts")
                .queryParam("userId", userId)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(PostDto.class)
            .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
            .doOnComplete(() -> log.info("Successfully fetched posts for user {}", userId))
            .doOnError(error -> log.error("Error fetching posts for user {}: {}", userId, error.getMessage()));
    }

    /**
     * Получает все посты с пагинацией.
     *
     * <p>Демонстрирует работу с query параметрами и pagination.</p>
     *
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return Mono с пагинированным ответом
     */
    public Mono<PagedResponseDto<PostDto>> getAllPostsPaginated(int page, int size) {
        log.debug("Fetching posts with pagination: page={}, size={}", page, size);

        int start = page * size;

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/posts")
                .queryParam("_start", start)
                .queryParam("_limit", size)
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<PostDto>>() {
            })
            .map(posts -> PagedResponseDto.<PostDto>builder()
                .data(posts)
                .page(page)
                .size(size)
                .total((long) posts.size()) // В реальном API было бы из заголовков
                .totalPages((posts.size() + size - 1) / size)
                .hasNext(posts.size() == size)
                .hasPrevious(page > 0)
                .timestamp(LocalDateTime.now())
                .build())
            .doOnSuccess(response -> log.info("Successfully fetched {} posts on page {}",
                response.getData().size(), page));
    }

    /**
     * Создает новый пост с валидацией.
     *
     * <p>Демонстрирует POST запрос с телом и обработку ответа.</p>
     *
     * @param createPostDto данные для создания поста
     * @return Mono с созданным постом
     */
    public Mono<PostDto> createPost(CreatePostDto createPostDto) {
        log.debug("Creating new post for user: {}", createPostDto.getUserId());

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .post()
            .uri("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(createPostDto))
            .retrieve()
            .bodyToMono(PostDto.class)
            .doOnSuccess(post -> log.info("Successfully created post with ID: {}", post.getId()))
            .doOnError(error -> log.error("Error creating post: {}", error.getMessage()));
    }

    /**
     * Обновляет существующий пост.
     *
     * <p>Демонстрирует PUT запрос для полного обновления ресурса.</p>
     *
     * @param postId        идентификатор поста для обновления
     * @param updatePostDto новые данные поста
     * @return Mono с обновленным постом
     */
    public Mono<PostDto> updatePost(Long postId, UpdatePostDto updatePostDto) {
        log.debug("Updating post with ID: {}", postId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .put()
            .uri("/posts/{id}", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(updatePostDto))
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals,
                response -> Mono.error(new RuntimeException("Post not found with ID: " + postId)))
            .bodyToMono(PostDto.class)
            .doOnSuccess(post -> log.info("Successfully updated post: {}", post.getId()))
            .doOnError(error -> log.error("Error updating post {}: {}", postId, error.getMessage()));
    }

    /**
     * Частично обновляет пост.
     *
     * <p>Демонстрирует PATCH запрос для частичного обновления.</p>
     *
     * @param postId  идентификатор поста
     * @param updates Map с полями для обновления
     * @return Mono с обновленным постом
     */
    public Mono<PostDto> patchPost(Long postId, Map<String, Object> updates) {
        log.debug("Partially updating post with ID: {}", postId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .patch()
            .uri("/posts/{id}", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(updates))
            .retrieve()
            .bodyToMono(PostDto.class)
            .doOnSuccess(post -> log.info("Successfully patched post: {}", post.getId()))
            .doOnError(error -> log.error("Error patching post {}: {}", postId, error.getMessage()));
    }

    /**
     * Удаляет пост по ID.
     *
     * <p>Демонстрирует DELETE запрос и обработку пустого ответа.</p>
     *
     * @param postId идентификатор поста для удаления
     * @return Mono<Void> подтверждающий удаление
     */
    public Mono<Void> deletePost(Long postId) {
        log.debug("Deleting post with ID: {}", postId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .delete()
            .uri("/posts/{id}", postId)
            .retrieve()
            .onStatus(HttpStatus.NOT_FOUND::equals,
                response -> Mono.error(new RuntimeException("Post not found with ID: " + postId)))
            .bodyToMono(Void.class)
            .doOnSuccess(unused -> log.info("Successfully deleted post: {}", postId))
            .doOnError(error -> log.error("Error deleting post {}: {}", postId, error.getMessage()));
    }

    /**
     * Получает комментарии к посту.
     *
     * <p>Демонстрирует работу с вложенными ресурсами.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    public Flux<CommentDto> getCommentsByPostId(Long postId) {
        log.debug("Fetching comments for post ID: {}", postId);

        // Можно подумать о .timeout(Duration.ofSeconds(10)); // Response timeout
        return jsonPlaceholderWebClient
            .get()
            .uri("/posts/{postId}/comments", postId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToFlux(CommentDto.class)
            .doOnComplete(() -> log.info("Successfully fetched comments for post {}", postId))
            .doOnError(error -> log.error("Error fetching comments for post {}: {}", postId, error.getMessage()));
    }

    /**
     * Метод восстановления для getUserById при исчерпании retry попыток.
     *
     * <p>Демонстрирует использование @Recover для graceful degradation.</p>
     *
     * @param ex     исключение, вызвавшее необходимость восстановления
     * @param userId идентификатор пользователя из оригинального вызова
     * @return Mono с пользователем по умолчанию или ошибкой
     */
    @Recover
    public Mono<UserDto> recoverGetUserById(Exception ex, Long userId) {
        log.warn("Recovering from getUserById failure for user {}: {}", userId, ex.getMessage());

        // Возвращаем пользователя по умолчанию или пробуем альтернативный источник
        return Mono.just(UserDto.builder()
            .id(userId)
            .username("unknown")
            .name("Unknown User")
            .email("unknown@example.com")
            .build());
    }

    /**
     * Выполняет множественные запросы параллельно.
     *
     * <p>Демонстрирует композицию нескольких reactive операций.</p>
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
     * Выполняет batch операцию для получения нескольких пользователей.
     *
     * <p>Демонстрирует параллельную обработку множественных запросов.</p>
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
}