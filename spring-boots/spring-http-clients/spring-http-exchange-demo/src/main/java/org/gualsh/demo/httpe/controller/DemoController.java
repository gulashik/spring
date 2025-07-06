package org.gualsh.demo.httpe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.httpe.dto.*;
import org.gualsh.demo.httpe.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/**
 * REST контроллер для демонстрации HTTP Exchange функциональности.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Этот контроллер демонстрирует интеграцию HTTP Exchange клиентов
 * в Spring WebFlux приложении:
 * </p>
 * <ul>
 * <li>Реактивные endpoints с Mono и Flux</li>
 * <li>Проксирование внешних API через собственное API</li>
 * <li>Обработка ошибок и возврат подходящих HTTP статусов</li>
 * <li>Валидация входных данных</li>
 * <li>Структурированные ответы API</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * // GET запросы
 * curl http://localhost:8080/api/demo/users
 * curl http://localhost:8080/api/demo/users/1
 * curl http://localhost:8080/api/demo/posts?userId=1
 *
 * // POST запрос
 * curl -X POST http://localhost:8080/api/demo/posts \
 *   -H "Content-Type: application/json" \
 *   -d '{"userId":1,"title":"New Post","body":"Content"}'
 * }</pre>
 *
 * <h4>Архитектурные принципы</h4>
 * <p>
 * Контроллер следует принципам:
 * </p>
 * <ul>
 * <li>Thin controller - минимум логики</li>
 * <li>Delegation - делегирование работы сервисному слою</li>
 * <li>Reactive streams - неблокирующие операции</li>
 * <li>HTTP semantics - правильное использование статус-кодов</li>
 * </ul>
 *
 * @author Образовательный проект
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Slf4j
public class DemoController {

    private final DemoService demoService;

    /**
     * Получает список всех пользователей из JSONPlaceholder.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует простейший case проксирования внешнего API:
     * </p>
     * <ul>
     * <li>Flux для возврата потока данных</li>
     * <li>Прямое делегирование сервисному слою</li>
     * <li>Автоматическая JSON сериализация</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запрос
     * GET /api/demo/users
     *
     * // Ответ
     * [
     *   {
     *     "id": 1,
     *     "name": "Leanne Graham",
     *     "email": "Sincere@april.biz"
     *   }
     * ]
     * }</pre>
     *
     * @return поток пользователей
     */
    @GetMapping("/users")
    public Flux<User> getUsers() {
        log.info("GET /api/demo/users - fetching all users");

        return demoService.getUserStatistics()
            .doOnNext(stats -> log.info("Total users available: {}", stats.getTotalUsers()))
            .then(demoService.getCombinedApiData())
            .flatMapMany(data -> Flux.fromIterable(data.getJsonPlaceholderUsers()));
    }

    /**
     * Получает конкретного пользователя по ID.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает обработку path variables и возврат ResponseEntity:
     * </p>
     * <ul>
     * <li>@PathVariable для извлечения ID из URL</li>
     * <li>Mono&lt;ResponseEntity&gt; для контроля HTTP статуса</li>
     * <li>map для преобразования данных</li>
     * <li>defaultIfEmpty для обработки отсутствующих данных</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запрос
     * GET /api/demo/users/1
     *
     * // Успешный ответ (200)
     * {
     *   "id": 1,
     *   "name": "Leanne Graham",
     *   "email": "Sincere@april.biz"
     * }
     *
     * // Пользователь не найден (404)
     * HTTP 404 Not Found
     * }</pre>
     *
     * @param id идентификатор пользователя
     * @return пользователь или 404
     */
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long id) {
        log.info("GET /api/demo/users/{} - fetching user", id);

        return demoService.getUserWithFallback(id)
            .map(user -> {
                if (user.getId().equals(-1L)) {
                    return ResponseEntity.notFound().<User>build();
                }
                return ResponseEntity.ok(user);
            })
            .doOnNext(response ->
                log.info("Returning user {} with status {}", id, response.getStatusCode()));
    }

    /**
     * Получает пользователя вместе с его постами.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует композитные операции и структурированные ответы:
     * </p>
     * <ul>
     * <li>Комбинирование нескольких HTTP-вызовов</li>
     * <li>Возврат сложных объектов</li>
     * <li>Обработка ошибок с fallback</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запрос
     * GET /api/demo/users/1/posts
     *
     * // Ответ
     * {
     *   "user": {
     *     "id": 1,
     *     "name": "Leanne Graham"
     *   },
     *   "posts": [
     *     {
     *       "id": 1,
     *       "title": "sunt aut facere"
     *     }
     *   ]
     * }
     * }</pre>
     *
     * @param id идентификатор пользователя
     * @return пользователь с постами
     */
    @GetMapping("/users/{id}/posts")
    public Mono<ResponseEntity<DemoService.UserWithPosts>> getUserWithPosts(@PathVariable Long id) {
        log.info("GET /api/demo/users/{}/posts - fetching user with posts", id);

        return demoService.getUserWithPosts(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .doOnNext(response ->
                log.info("Returning user {} posts with status {}", id, response.getStatusCode()));
    }

    /**
     * Получает популярные посты.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает работу с query parameters и аналитическими запросами:
     * </p>
     * <ul>
     * <li>@RequestParam с default значением</li>
     * <li>Аналитические операции над потоками данных</li>
     * <li>Ограничение результатов</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запросы
     * GET /api/demo/posts/popular          // top 10
     * GET /api/demo/posts/popular?limit=5  // top 5
     *
     * // Ответ
     * [
     *   {
     *     "post": {
     *       "id": 1,
     *       "title": "sunt aut facere"
     *     },
     *     "commentCount": 5
     *   }
     * ]
     * }</pre>
     *
     * @param limit количество популярных постов (по умолчанию 10)
     * @return поток популярных постов
     */
    @GetMapping("/posts/popular")
    public Flux<DemoService.PostWithCommentCount> getPopularPosts(
        @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/demo/posts/popular?limit={} - fetching popular posts", limit);

        return demoService.getPopularPosts(limit)
            .doOnComplete(() ->
                log.info("Completed returning {} popular posts", limit));
    }

    /**
     * Создает новый пост.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует создание ресурсов через HTTP Exchange:
     * </p>
     * <ul>
     * <li>@PostMapping для HTTP POST</li>
     * <li>@Valid для валидации входных данных</li>
     * <li>@RequestBody для получения JSON</li>
     * <li>ResponseEntity для контроля статуса (201 Created)</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запрос
     * POST /api/demo/posts
     * Content-Type: application/json
     *
     * {
     *   "userId": 1,
     *   "title": "New Post",
     *   "body": "Post content"
     * }
     *
     * // Успешный ответ (201)
     * {
     *   "id": 101,
     *   "userId": 1,
     *   "title": "New Post",
     *   "body": "Post content"
     * }
     * }</pre>
     *
     * @param request данные для создания поста
     * @return созданный пост
     */
    @PostMapping("/posts")
    public Mono<ResponseEntity<Post>> createPost(@Valid @RequestBody CreatePostRequest request) {
        log.info("POST /api/demo/posts - creating post for user {}: {}",
            request.getUserId(), request.getTitle());

        return demoService.createValidatedPost(request)
            .map(post -> ResponseEntity.status(201).body(post))
            .doOnNext(response ->
                log.info("Created post with status {}", response.getStatusCode()));
    }

    /**
     * Получает статистику по пользователям.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает аналитические endpoints:
     * </p>
     * <ul>
     * <li>Агрегация данных из нескольких источников</li>
     * <li>Вычисления над потоками данных</li>
     * <li>Возврат структурированной статистики</li>
     * </ul>
     *
     * <pre>{@code
     * // HTTP запрос
     * GET /api/demo/statistics
     *
     * // Ответ
     * {
     *   "totalUsers": 10,
     *   "totalPosts": 100,
     *   "averagePostsPerUser": 10.0,
     *   "maxPostsPerUser": 15,
     *   "mostActiveUser": "Leanne Graham"
     * }
     * }</pre>
     *
     * @return статистика пользователей
     */
    @GetMapping("/statistics")
    public Mono<DemoService.UserStatistics> getUserStatistics() {
        log.info("GET /api/demo/statistics - calculating user statistics");

        return demoService.getUserStatistics()
            .doOnNext(stats ->
                log.info("Returning statistics: {} users, {} posts",
                    stats.getTotalUsers(), stats.getTotalPosts()));
    }

    /**
     * Получает данные из ReqRes API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует работу с различными внешними API:
     * </p>
     * <ul>
     * <li>Интеграция с множественными источниками данных</li>
     * <li>Различные форматы ответов</li>
     * <li>Трансформация данных между форматами</li>
     * </ul>
     *
     * @return пользователи из ReqRes API
     */
    @GetMapping("/reqres-users")
    public Flux<ReqResUser> getReqResUsers() {
        log.info("GET /api/demo/reqres-users - fetching users from ReqRes API");

        return demoService.getReqResUsers()
            .doOnComplete(() ->
                log.info("Completed returning ReqRes users"));
    }

    /**
     * Получает объединенные данные из разных API.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает агрегацию данных из множественных источников:
     * </p>
     * <ul>
     * <li>Параллельные вызовы к разным API</li>
     * <li>Объединение результатов</li>
     * <li>Единый endpoint для композитных данных</li>
     * </ul>
     *
     * @return объединенные данные
     */
    @GetMapping("/combined-data")
    public Mono<DemoService.CombinedApiData> getCombinedData() {
        log.info("GET /api/demo/combined-data - fetching combined API data");

        return demoService.getCombinedApiData()
            .doOnNext(data ->
                log.info("Returning combined data: {} + {} users from different APIs",
                    data.getJsonPlaceholderUsers().size(),
                    data.getReqResUsers().size()));
    }

    /**
     * Health check endpoint для мониторинга.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Простой endpoint для проверки работоспособности:
     * </p>
     * <ul>
     * <li>Быстрая проверка статуса приложения</li>
     * <li>Информация о версии и конфигурации</li>
     * <li>Полезен для load balancers и мониторинга</li>
     * </ul>
     *
     * @return статус приложения
     */
    @GetMapping("/health")
    public Mono<ApiResponse<String>> getHealth() {
        log.debug("GET /api/demo/health - health check");

        return Mono.just(ApiResponse.success("HTTP Exchange Demo is running"))
            .doOnNext(response ->
                log.debug("Health check completed successfully"));
    }
}
