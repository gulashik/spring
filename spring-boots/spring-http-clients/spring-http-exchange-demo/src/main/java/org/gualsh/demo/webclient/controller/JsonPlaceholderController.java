package org.gualsh.demo.webclient.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.*;
import org.gualsh.demo.webclient.service.JsonPlaceholderService;
import org.gualsh.demo.webclient.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для демонстрации возможностей @HttpExchange с JSONPlaceholder API.
 *
 * <p>Изменения в контроллере минимальны, поскольку HttpExchange изменения
 * затрагивают в основном слой сервисов. Контроллер продолжает работать
 * с теми же интерфейсами сервисов.</p>
 *
 * <p>Преимущества архитектуры с HttpExchange:</p>
 * <ul>
 *   <li>Контроллеры остаются неизменными</li>
 *   <li>Сервисы упрощаются и фокусируются на бизнес-логике</li>
 *   <li>HTTP детали инкапсулированы в клиентских интерфейсах</li>
 *   <li>Легче тестировать каждый слой отдельно</li>
 * </ul>
 *
 * @author Demo
 * @version 2.0
 * @see JsonPlaceholderService
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jsonplaceholder")
@RequiredArgsConstructor
@Validated
public class JsonPlaceholderController {

    private final JsonPlaceholderService jsonPlaceholderService;

    /**
     * Получает список всех пользователей.
     *
     * <p>Функциональность контроллера остается неизменной.
     * Все улучшения скрыты в сервисном слое.</p>
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<UserDto>>> getAllUsers() {
        log.info("REST: Getting all users");

        return jsonPlaceholderService.getAllUsers()
            .map(users -> {
                log.info("REST: Returning {} users", users.size());
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(users.size()))
                    .header("X-Client-Type", "HttpExchange")
                    .body(users);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает пользователя по ID.
     */
    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<UserDto>> getUserById(
        @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST: Getting user by ID: {}", userId);

        return jsonPlaceholderService.getUserById(userId)
            .map(user -> {
                log.info("REST: Found user: {}", user.getUsername());
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(user);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает посты пользователя как стрим.
     */
    @GetMapping(value = "/users/{userId}/posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PostDto> getUserPosts(@PathVariable Long userId) {
        log.info("REST: Getting posts for user: {}", userId);

        return jsonPlaceholderService.getPostsByUserId(userId)
            .doOnNext(post -> log.debug("REST: Streaming post: {}", post.getTitle()));
    }

    /**
     * Получает посты с пагинацией.
     */
    @GetMapping("/posts")
    public Mono<ResponseEntity<PagedResponseDto<PostDto>>> getPostsPaginated(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("REST: Getting posts with pagination: page={}, size={}", page, size);

        return jsonPlaceholderService.getAllPostsPaginated(page, size)
            .map(pagedResponse -> {
                log.info("REST: Returning {} posts on page {}",
                    pagedResponse.getData().size(), page);
                return ResponseEntity.ok()
                    .header("X-Page", String.valueOf(page))
                    .header("X-Size", String.valueOf(size))
                    .header("X-Total", String.valueOf(pagedResponse.getTotal()))
                    .header("X-Client-Type", "HttpExchange")
                    .body(pagedResponse);
            });
    }

    /**
     * Создает новый пост.
     */
    @PostMapping("/posts")
    public Mono<ResponseEntity<PostDto>> createPost(@Valid @RequestBody CreatePostDto createPostDto) {
        log.info("REST: Creating new post for user: {}", createPostDto.getUserId());

        return jsonPlaceholderService.createPost(createPostDto)
            .map(post -> {
                log.info("REST: Created post with ID: {}", post.getId());
                return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/v1/jsonplaceholder/posts/" + post.getId())
                    .header("X-Client-Type", "HttpExchange")
                    .body(post);
            })
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Обновляет существующий пост полностью.
     */
    @PutMapping("/posts/{postId}")
    public Mono<ResponseEntity<PostDto>> updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody UpdatePostDto updatePostDto) {
        log.info("REST: Updating post: {}", postId);

        updatePostDto.setId(postId);

        return jsonPlaceholderService.updatePost(postId, updatePostDto)
            .map(post -> {
                log.info("REST: Updated post: {}", post.getId());
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(post);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Частично обновляет пост.
     */
    @PatchMapping("/posts/{postId}")
    public Mono<ResponseEntity<PostDto>> patchPost(
        @PathVariable Long postId,
        @RequestBody Map<String, Object> updates) {
        log.info("REST: Patching post: {} with updates: {}", postId, updates.keySet());

        return jsonPlaceholderService.patchPost(postId, updates)
            .map(post -> ResponseEntity.ok()
                .header("X-Client-Type", "HttpExchange")
                .body(post))
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Удаляет пост.
     */
    @DeleteMapping("/posts/{postId}")
    public Mono<ResponseEntity<Void>> deletePost(@PathVariable Long postId) {
        log.info("REST: Deleting post: {}", postId);

        return jsonPlaceholderService.deletePost(postId)
            .map(unused -> {
                log.info("REST: Deleted post: {}", postId);
                return ResponseEntity.noContent()
                    .header("X-Client-Type", "HttpExchange")
                    .<Void>build();
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает комментарии к посту.
     */
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CommentDto> getPostComments(@PathVariable Long postId) {
        log.info("REST: Getting comments for post: {}", postId);

        return jsonPlaceholderService.getCommentsByPostId(postId)
            .doOnNext(comment -> log.debug("REST: Found comment from: {}", comment.getEmail()));
    }

    /**
     * Получает пользователя с его постами.
     */
    @GetMapping("/users/{userId}/profile")
    public Mono<ResponseEntity<Map<String, Object>>> getUserProfile(@PathVariable Long userId) {
        log.info("REST: Getting user profile: {}", userId);

        return jsonPlaceholderService.getUserWithPosts(userId)
            .map(profile -> {
                log.info("REST: Returning profile for user: {}", userId);
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(profile);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает множественных пользователей за один запрос.
     */
    @PostMapping(value = "/users/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserDto> getUsersBatch(@RequestBody List<Long> userIds) {
        log.info("REST: Getting batch of users: {}", userIds);

        return jsonPlaceholderService.getUsersBatch(userIds)
            .doOnNext(user -> log.debug("REST: Streaming user: {}", user.getUsername()));
    }

    /**
     * Получает конкретный пост по ID.
     *
     * <p>Новый endpoint, демонстрирующий дополнительную функциональность.</p>
     */
    @GetMapping("/posts/{postId}")
    public Mono<ResponseEntity<PostDto>> getPostById(@PathVariable Long postId) {
        log.info("REST: Getting post by ID: {}", postId);

        return jsonPlaceholderService.getPostById(postId)
            .map(post -> {
                log.info("REST: Found post: {}", post.getTitle());
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(post);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает все посты без фильтрации.
     *
     * <p>Альтернативный endpoint для получения всех постов.</p>
     */
    @GetMapping("/posts/all")
    public Mono<ResponseEntity<List<PostDto>>> getAllPosts() {
        log.info("REST: Getting all posts");

        return jsonPlaceholderService.getAllPosts()
            .map(posts -> {
                log.info("REST: Returning {} posts", posts.size());
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(posts.size()))
                    .header("X-Client-Type", "HttpExchange")
                    .body(posts);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Альтернативный способ получения комментариев через query параметр.
     */
    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CommentDto> getCommentsByPostQuery(@RequestParam Long postId) {
        log.info("REST: Getting comments for post via query: {}", postId);

        return jsonPlaceholderService.getCommentsByPostIdAlternative(postId)
            .doOnNext(comment -> log.debug("REST: Found comment via query from: {}", comment.getEmail()));
    }
}

/**
 * REST контроллер для демонстрации Weather API с HttpExchange.
 *
 * @author Demo
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Validated
class WeatherController {

    private final WeatherService weatherService;

    /**
     * Получает текущую погоду для города.
     */
    @GetMapping("/current")
    public Mono<ResponseEntity<WeatherDto>> getCurrentWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting weather for city: {}", city);

        return weatherService.getCurrentWeather(city)
            .map(weather -> {
                log.info("REST: Found weather for {}: {}°C",
                    city, weather.getMain() != null ? weather.getMain().getTemp() : "N/A");
                return ResponseEntity.ok()
                    .header("X-Cache-Key", city.toLowerCase())
                    .header("X-Client-Type", "HttpExchange")
                    .body(weather);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает погоду по координатам.
     */
    @GetMapping("/coordinates")
    public Mono<ResponseEntity<WeatherDto>> getWeatherByCoordinates(
        @RequestParam double lat,
        @RequestParam double lon) {
        log.info("REST: Getting weather by coordinates: lat={}, lon={}", lat, lon);

        return weatherService.getCurrentWeatherByCoordinates(lat, lon)
            .map(weather -> ResponseEntity.ok()
                .header("X-Client-Type", "HttpExchange")
                .body(weather))
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает детальную информацию о погоде.
     */
    @GetMapping("/detailed")
    public Mono<ResponseEntity<WeatherDto>> getDetailedWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting detailed weather for city: {}", city);

        return weatherService.getDetailedWeather(city)
            .map(weather -> ResponseEntity.ok()
                .header("X-Client-Type", "HttpExchange")
                .body(weather))
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Проверяет доступность weather сервиса.
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkWeatherServiceHealth() {
        log.info("REST: Checking weather service health");

        return weatherService.isWeatherServiceAvailable()
            .map(available -> {
                Map<String, Object> status = Map.of(
                    "service", "weather-api",
                    "available", available,
                    "client-type", "HttpExchange",
                    "timestamp", System.currentTimeMillis()
                );

                HttpStatus httpStatus = available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                return ResponseEntity.status(httpStatus).body(status);
            });
    }

    /**
     * Получает погоду для нескольких городов.
     *
     * <p>Новый endpoint, демонстрирующий композицию запросов.</p>
     */
    @PostMapping(value = "/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WeatherDto> getWeatherForCities(@RequestBody List<String> cities) {
        log.info("REST: Getting weather for {} cities", cities.size());

        return weatherService.getWeatherForMultipleCities(cities)
            .doOnNext(weather -> log.debug("REST: Streaming weather for: {}", weather.getName()));
    }

    /**
     * Сравнивает погоду между двумя городами.
     *
     * <p>Демонстрирует бизнес-логику с использованием нескольких API вызовов.</p>
     */
    @GetMapping("/compare")
    public Mono<ResponseEntity<Map<String, Object>>> compareWeather(
        @RequestParam @NotNull String city1,
        @RequestParam @NotNull String city2) {
        log.info("REST: Comparing weather between {} and {}", city1, city2);

        return weatherService.compareWeather(city1, city2)
            .map(comparison -> {
                log.info("REST: Weather comparison completed for {} vs {}", city1, city2);
                return ResponseEntity.ok()
                    .header("X-Client-Type", "HttpExchange")
                    .body(comparison);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }
}