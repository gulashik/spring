package org.gualsh.demo.httpe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.httpe.client.JsonPlaceholderClient;
import org.gualsh.demo.httpe.client.ReqResClient;
import org.gualsh.demo.httpe.dto.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Сервисный слой для демонстрации работы с HTTP Exchange клиентами.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот сервис демонстрирует лучшие практики использования HTTP Exchange клиентов:
 * </p>
 * <ul>
 * <li>Композиция нескольких HTTP-вызовов</li>
 * <li>Обработка ошибок и retry логика</li>
 * <li>Трансформация данных между различными API</li>
 * <li>Реактивное программирование с Mono и Flux</li>
 * <li>Логирование и мониторинг HTTP-вызовов</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * @Autowired
 * private DemoService demoService;
 *
 * // Получение пользователя с его постами
 * Mono<UserWithPosts> result = demoService.getUserWithPosts(1L);
 *
 * // Подписка на результат
 * result.subscribe(
 *     userWithPosts -> System.out.println("User: " + userWithPosts.getUser().getName()),
 *     error -> System.err.println("Error: " + error.getMessage())
 * );
 * }</pre>
 *
 * <h4>Архитектурные принципы</h4>
 * <p>
 * Сервис следует принципам:
 * </p>
 * <ul>
 * <li>Single Responsibility - каждый метод решает одну задачу</li>
 * <li>Composition over inheritance - композиция HTTP-клиентов</li>
 * <li>Fail-fast principle - быстрое обнаружение ошибок</li>
 * <li>Reactive programming - неблокирующие операции</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DemoService {

    private final JsonPlaceholderClient jsonPlaceholderClient;
    private final ReqResClient reqResClient;

    /**
     * Получает пользователя вместе с его постами.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует композицию HTTP-вызовов с использованием реактивных операторов:
     * </p>
     * <ul>
     * <li>flatMap для последовательных вызовов</li>
     * <li>zip для параллельных вызовов</li>
     * <li>map для трансформации данных</li>
     * <li>doOnNext для side effects (логирование)</li>
     * </ul>
     *
     * <pre>{@code
     * // Последовательность операций:
     * // 1. Получение пользователя
     * // 2. Параллельное получение постов пользователя
     * // 3. Объединение результатов
     * Mono<UserWithPosts> result = demoService.getUserWithPosts(1L);
     * }</pre>
     *
     * <h4>Обработка ошибок</h4>
     * <p>
     * Используется retry механизм для повышения надежности:
     * </p>
     * <ul>
     * <li>Exponential backoff для retry</li>
     * <li>Максимальное количество попыток</li>
     * <li>Логирование ошибок</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return пользователь с его постами
     */
    public Mono<UserWithPosts> getUserWithPosts(Long userId) {
        log.info("Fetching user {} with posts", userId);

        return jsonPlaceholderClient.getUser(userId)
            .doOnNext(user -> log.debug("Found user: {}", user.getName()))
            .flatMap(user -> {
                // Параллельное получение постов пользователя
                Flux<Post> userPosts = jsonPlaceholderClient.getPosts(userId)
                    .doOnNext(post -> log.debug("Found post: {}", post.title()));

                // Объединение пользователя и его постов
                return userPosts.collectList()
                    .map(posts -> UserWithPosts.builder()
                        .user(user)
                        .posts(posts)
                        .build());
            })
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .doBeforeRetry(retrySignal ->
                    log.warn("Retrying getUserWithPosts for user {} (attempt {})",
                        userId, retrySignal.totalRetries() + 1)))
            .doOnSuccess(result ->
                log.info("Successfully fetched user {} with {} posts",
                    userId, result.getPosts().size()))
            .doOnError(error ->
                log.error("Failed to fetch user {} with posts: {}",
                    userId, error.getMessage()));
    }

    /**
     * Получает популярные посты (с наибольшим количеством комментариев).
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует сложную обработку потоков данных:
     * </p>
     * <ul>
     * <li>Получение всех постов</li>
     * <li>Параллельное получение комментариев для каждого поста</li>
     * <li>Сортировка по количеству комментариев</li>
     * <li>Ограничение результата топ-N</li>
     * </ul>
     *
     * <pre>{@code
     * // Получение топ-5 популярных постов
     * Flux<PostWithCommentCount> popularPosts = demoService.getPopularPosts(5);
     *
     * popularPosts.subscribe(post ->
     *     System.out.println(post.getPost().title() + " - " + post.getCommentCount() + " comments"));
     * }</pre>
     *
     * <h4>Оптимизация производительности</h4>
     * <p>
     * Используются техники оптимизации:
     * </p>
     * <ul>
     * <li>Параллельная обработка с ограничением concurrency</li>
     * <li>Ограничение количества обрабатываемых постов</li>
     * <li>Эффективная сортировка в потоке</li>
     * </ul>
     *
     * @param limit количество популярных постов
     * @return поток популярных постов
     */
    public Flux<PostWithCommentCount> getPopularPosts(int limit) {
        log.info("Fetching top {} popular posts", limit);

        return jsonPlaceholderClient.getPosts(null)
            .take(20) // Ограничиваем для демонстрации
            .flatMap(post -> {
                // Получаем количество комментариев для каждого поста
                return jsonPlaceholderClient.getPostComments(post.id())
                    .count()
                    .map(commentCount -> PostWithCommentCount.builder()
                        .post(post)
                        .commentCount(commentCount.intValue())
                        .build());
            }, 5) // Ограничиваем concurrency до 5
            .sort((p1, p2) -> Integer.compare(p2.getCommentCount(), p1.getCommentCount()))
            .take(limit)
            .doOnNext(post ->
                log.debug("Popular post: {} with {} comments",
                    post.getPost().title(), post.getCommentCount()))
            .doOnComplete(() ->
                log.info("Completed fetching top {} popular posts", limit));
    }

    /**
     * Создает новый пост с валидацией.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает как добавлять бизнес-логику при работе с HTTP-клиентами:
     * </p>
     * <ul>
     * <li>Валидация входных данных</li>
     * <li>Проверка существования связанных ресурсов</li>
     * <li>Создание ресурса</li>
     * <li>Обработка ошибок создания</li>
     * </ul>
     *
     * <pre>{@code
     * CreatePostRequest request = CreatePostRequest.builder()
     *     .userId(1L)
     *     .title("New Post")
     *     .body("Post content")
     *     .build();
     *
     * Mono<Post> newPost = demoService.createValidatedPost(request);
     * }</pre>
     *
     * @param request данные для создания поста
     * @return созданный пост
     */
    public Mono<Post> createValidatedPost(CreatePostRequest request) {
        log.info("Creating post for user {}: {}", request.getUserId(), request.getTitle());

        return jsonPlaceholderClient.getUser(request.getUserId())
            .doOnNext(user -> log.debug("Validated user exists: {}", user.getName()))
            .then(jsonPlaceholderClient.createPost(request))
            .doOnSuccess(post ->
                log.info("Successfully created post {}: {}", post.id(), post.title()))
            .doOnError(error ->
                log.error("Failed to create post for user {}: {}",
                    request.getUserId(), error.getMessage()));
    }

    /**
     * Демонстрирует работу с другим API (ReqRes).
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает как работать с различными API в одном приложении:
     * </p>
     * <ul>
     * <li>Разные структуры ответов</li>
     * <li>Различные паттерны пагинации</li>
     * <li>Трансформация между форматами</li>
     * </ul>
     *
     * @return список пользователей из ReqRes API
     */
    public Flux<ReqResUser> getReqResUsers() {
        log.info("Fetching users from ReqRes API");

        return reqResClient.getUsers(1, 6)
            .doOnNext(response ->
                log.debug("Received page {} of {} total pages",
                    response.getPage(), response.getTotalPages()))
            .flatMapMany(response -> Flux.fromIterable(response.getData()))
            .doOnNext(user ->
                log.debug("ReqRes user: {} {}", user.getFirstName(), user.getLastName()))
            .doOnComplete(() ->
                log.info("Completed fetching ReqRes users"));
    }

    /**
     * Симулирует обработку ошибок и fallback механизмы.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует паттерны обработки ошибок в реактивных приложениях:
     * </p>
     * <ul>
     * <li>onErrorReturn для fallback значений</li>
     * <li>onErrorResume для альтернативных источников данных</li>
     * <li>timeout для контроля времени выполнения</li>
     * <li>retry с различными стратегиями</li>
     * </ul>
     *
     * <pre>{@code
     * // Получение пользователя с fallback
     * Mono<User> user = demoService.getUserWithFallback(999L);
     *
     * // Результат будет либо пользователь, либо fallback значение
     * }</pre>
     *
     * @param userId идентификатор пользователя
     * @return пользователь или fallback
     */
    public Mono<User> getUserWithFallback(Long userId) {
        log.info("Fetching user {} with fallback", userId);

        return jsonPlaceholderClient.getUser(userId)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
            .onErrorReturn(createFallbackUser(userId))
            .doOnNext(user -> {
                if (user.getId().equals(-1L)) {
                    log.warn("Using fallback user for ID {}", userId);
                } else {
                    log.info("Successfully fetched user {}", user.getName());
                }
            });
    }

    /**
     * Создает fallback пользователя.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Fallback механизмы обеспечивают graceful degradation
     * при недоступности внешних сервисов.
     * </p>
     *
     * @param requestedId запрошенный ID
     * @return fallback пользователь
     */
    private User createFallbackUser(Long requestedId) {
        return User.builder()
            .id(-1L)
            .name("Fallback User")
            .username("fallback")
            .email("fallback@example.com")
            .build();
    }

    /**
     * Демонстрирует пакетную обработку данных.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает техники эффективной обработки больших объемов данных:
     * </p>
     * <ul>
     * <li>Пакетная обработка с ограничением размера</li>
     * <li>Контроль concurrency</li>
     * <li>Агрегация результатов</li>
     * </ul>
     *
     * @return статистика по пользователям
     */
    public Mono<UserStatistics> getUserStatistics() {
        log.info("Calculating user statistics");

        return jsonPlaceholderClient.getUsers()
            .buffer(3) // Обрабатываем пользователей группами по 3
            .flatMap(userBatch -> {
                // Параллельная обработка каждой группы
                return Flux.fromIterable(userBatch)
                    .flatMap(user -> jsonPlaceholderClient.getPosts(user.getId())
                        .count()
                        .map(postCount -> UserPostCount.builder()
                            .user(user)
                            .postCount(postCount.intValue())
                            .build()))
                    .collectList();
            }, 2) // Ограничиваем concurrency групп
            .flatMap(Flux::fromIterable)
            .reduce(UserStatistics.builder().build(), (stats, userPostCount) -> {
                stats.setTotalUsers(stats.getTotalUsers() + 1);
                stats.setTotalPosts(stats.getTotalPosts() + userPostCount.getPostCount());

                if (userPostCount.getPostCount() > stats.getMaxPostsPerUser()) {
                    stats.setMaxPostsPerUser(userPostCount.getPostCount());
                    stats.setMostActiveUser(userPostCount.getUser().getName());
                }

                return stats;
            })
            .map(stats -> {
                if (stats.getTotalUsers() > 0) {
                    stats.setAveragePostsPerUser(
                        (double) stats.getTotalPosts() / stats.getTotalUsers());
                }
                return stats;
            })
            .doOnSuccess(stats ->
                log.info("User statistics: {} users, {} posts, avg {:.1f} posts/user",
                    stats.getTotalUsers(), stats.getTotalPosts(), stats.getAveragePostsPerUser()));
    }

    /**
     * Демонстрирует комбинирование данных из разных API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает как объединять данные из различных источников:
     * </p>
     * <ul>
     * <li>Параллельные вызовы к разным API</li>
     * <li>Zip операторы для объединения результатов</li>
     * <li>Трансформация между различными форматами данных</li>
     * <li>Обработка несовместимых структур данных</li>
     * </ul>
     *
     * <pre>{@code
     * // Получение объединенных данных
     * Mono<CombinedApiData> combined = demoService.getCombinedApiData();
     *
     * combined.subscribe(data -> {
     *     System.out.println("JSONPlaceholder users: " + data.getJsonPlaceholderUsers().size());
     *     System.out.println("ReqRes users: " + data.getReqResUsers().size());
     * });
     * }</pre>
     *
     * @return объединенные данные из разных API
     */
    public Mono<CombinedApiData> getCombinedApiData() {
        log.info("Combining data from multiple APIs");

        Mono<java.util.List<User>> jsonPlaceholderUsers = jsonPlaceholderClient.getUsers()
            .take(5)
            .collectList()
            .doOnNext(users -> log.debug("Fetched {} JSONPlaceholder users", users.size()));

        Mono<java.util.List<ReqResUser>> reqResUsers = reqResClient.getUsers(1, 5)
            .map(ReqResListResponse::getData)
            .doOnNext(users -> log.debug("Fetched {} ReqRes users", users.size()));

        return Mono.zip(jsonPlaceholderUsers, reqResUsers)
            .map(tuple -> CombinedApiData.builder()
                .jsonPlaceholderUsers(tuple.getT1())
                .reqResUsers(tuple.getT2())
                .combinedAt(java.time.LocalDateTime.now())
                .build())
            .doOnSuccess(data ->
                log.info("Successfully combined data: {} + {} users",
                    data.getJsonPlaceholderUsers().size(),
                    data.getReqResUsers().size()));
    }

    /**
     * Вспомогательные классы для демонстрации.
     */
    @lombok.Data
    @lombok.Builder
    public static class UserWithPosts {
        private User user;
        private java.util.List<Post> posts;
    }

    @lombok.Data
    @lombok.Builder
    public static class PostWithCommentCount {
        private Post post;
        private int commentCount;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserPostCount {
        private User user;
        private int postCount;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserStatistics {
        @lombok.Builder.Default
        private int totalUsers = 0;
        @lombok.Builder.Default
        private int totalPosts = 0;
        @lombok.Builder.Default
        private double averagePostsPerUser = 0.0;
        @lombok.Builder.Default
        private int maxPostsPerUser = 0;
        private String mostActiveUser;
    }

    @lombok.Data
    @lombok.Builder
    public static class CombinedApiData {
        private java.util.List<User> jsonPlaceholderUsers;
        private java.util.List<ReqResUser> reqResUsers;
        private java.time.LocalDateTime combinedAt;
    }
}
