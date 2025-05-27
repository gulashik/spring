package org.gualsh.demo.webclient.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.webclient.dto.*;
import org.gualsh.demo.webclient.service.JsonPlaceholderService;
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
 * REST контроллер для демонстрации возможностей WebClient с JSONPlaceholder API.
 *
 * <p>Предоставляет endpoints для:</p>
 * <ul>
 *   <li>CRUD операции с пользователями и постами</li>
 *   <li>Демонстрации различных HTTP методов</li>
 *   <li>Работы с reactive типами (Mono, Flux)</li>
 *   <li>Обработки ошибок и валидации</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
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
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>GET запрос с кэшированием</li>
     *   <li>Возврат Mono&lt;List&gt;</li>
     *   <li>Content negotiation</li>
     * </ul>
     *
     * @return ResponseEntity с списком пользователей
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<UserDto>>> getAllUsers() {
        log.info("REST: Getting all users");

        return jsonPlaceholderService.getAllUsers()
            .map(users -> {
                log.info("REST: Returning {} users", users.size());
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(users.size()))
                    .body(users);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает пользователя по ID.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>Path variables</li>
     *   <li>Валидацию параметров</li>
     *   <li>Обработку 404 ошибок</li>
     * </ul>
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity с пользователем или 404
     */
    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<UserDto>> getUserById(
        @PathVariable @NotNull @Min(1) Long userId) {
        log.info("REST: Getting user by ID: {}", userId);

        return jsonPlaceholderService.getUserById(userId)
            .map(user -> {
                log.info("REST: Found user: {}", user.getUsername());
                return ResponseEntity.ok(user);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает посты пользователя.
     *
     * <p>Демонстрирует работу с Flux и Server-Sent Events.</p>
     *
     * @param userId идентификатор пользователя
     * @return Flux с постами в виде text/event-stream
     */
    @GetMapping(value = "/users/{userId}/posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PostDto> getUserPosts(@PathVariable Long userId) {
        log.info("REST: Getting posts for user: {}", userId);

        return jsonPlaceholderService.getPostsByUserId(userId)
            .doOnNext(post -> log.debug("REST: Streaming post: {}", post.getTitle()));
    }

    /**
     * Получает посты с пагинацией.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>Query parameters с значениями по умолчанию</li>
     *   <li>Пагинированные ответы</li>
     *   <li>Валидацию параметров</li>
     * </ul>
     *
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return ResponseEntity с пагинированными постами
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
                    .body(pagedResponse);
            });
    }

    /**
     * Создает новый пост.
     *
     * <p>Демонстрирует:</p>
     * <ul>
     *   <li>POST запрос с валидацией тела</li>
     *   <li>Возврат 201 Created</li>
     *   <li>Location header</li>
     * </ul>
     *
     * @param createPostDto данные для создания поста
     * @return ResponseEntity с созданным постом
     */
    @PostMapping("/posts")
    public Mono<ResponseEntity<PostDto>> createPost(@Valid @RequestBody CreatePostDto createPostDto) {
        log.info("REST: Creating new post for user: {}", createPostDto.getUserId());

        return jsonPlaceholderService.createPost(createPostDto)
            .map(post -> {
                log.info("REST: Created post with ID: {}", post.getId());
                return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/v1/jsonplaceholder/posts/" + post.getId())
                    .body(post);
            });
    }

    /**
     * Обновляет существующий пост полностью.
     *
     * <p>Демонстрирует PUT запрос для полного обновления.</p>
     *
     * @param postId идентификатор поста
     * @param updatePostDto новые данные поста
     * @return ResponseEntity с обновленным постом
     */
    @PutMapping("/posts/{postId}")
    public Mono<ResponseEntity<PostDto>> updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody UpdatePostDto updatePostDto) {
        log.info("REST: Updating post: {}", postId);

        // Устанавливаем ID из path parameter
        updatePostDto.setId(postId);

        return jsonPlaceholderService.updatePost(postId, updatePostDto)
            .map(post -> {
                log.info("REST: Updated post: {}", post.getId());
                return ResponseEntity.ok(post);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Частично обновляет пост.
     *
     * <p>Демонстрирует PATCH запрос с Map параметрами.</p>
     *
     * @param postId идентификатор поста
     * @param updates Map с полями для обновления
     * @return ResponseEntity с обновленным постом
     */
    @PatchMapping("/posts/{postId}")
    public Mono<ResponseEntity<PostDto>> patchPost(
        @PathVariable Long postId,
        @RequestBody Map<String, Object> updates) {
        log.info("REST: Patching post: {} with updates: {}", postId, updates.keySet());

        return jsonPlaceholderService.patchPost(postId, updates)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Удаляет пост.
     *
     * <p>Демонстрирует DELETE запрос с возвратом 204 No Content.</p>
     *
     * @param postId идентификатор поста для удаления
     * @return ResponseEntity без содержимого
     */
    @DeleteMapping("/posts/{postId}")
    public Mono<ResponseEntity<Void>> deletePost(@PathVariable Long postId) {
        log.info("REST: Deleting post: {}", postId);

        return jsonPlaceholderService.deletePost(postId)
            .map(unused -> {
                log.info("REST: Deleted post: {}", postId);
                return ResponseEntity.noContent().<Void>build();
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает комментарии к посту.
     *
     * <p>Демонстрирует работу с вложенными ресурсами и Flux.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    @GetMapping(value = "/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CommentDto> getPostComments(@PathVariable Long postId) {
        log.info("REST: Getting comments for post: {}", postId);

        return jsonPlaceholderService.getCommentsByPostId(postId)
            .doOnNext(comment -> log.debug("REST: Found comment from: {}", comment.getEmail()));
    }

    /**
     * Получает пользователя с его постами.
     *
     * <p>Демонстрирует композицию нескольких запросов.</p>
     *
     * @param userId идентификатор пользователя
     * @return ResponseEntity с объединенными данными
     */
    @GetMapping("/users/{userId}/profile")
    public Mono<ResponseEntity<Map<String, Object>>> getUserProfile(@PathVariable Long userId) {
        log.info("REST: Getting user profile: {}", userId);

        return jsonPlaceholderService.getUserWithPosts(userId)
            .map(profile -> {
                log.info("REST: Returning profile for user: {}", userId);
                return ResponseEntity.ok(profile);
            })
            .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Получает множественных пользователей за один запрос.
     *
     * <p>Демонстрирует batch операции.</p>
     *
     * @param userIds список идентификаторов пользователей
     * @return Flux с пользователями
     */
    @PostMapping(value = "/users/batch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserDto> getUsersBatch(@RequestBody List<Long> userIds) {
        log.info("REST: Getting batch of users: {}", userIds);

        return jsonPlaceholderService.getUsersBatch(userIds)
            .doOnNext(user -> log.debug("REST: Streaming user: {}", user.getUsername()));
    }
}

/**
 * REST контроллер для демонстрации Weather API.
 *
 * @author Demo
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Validated
class WeatherController {

    private final org.gualsh.demo.webclient.service.WeatherService weatherService;

    /**
     * Получает текущую погоду для города.
     *
     * <p>Демонстрирует работу с внешним API, требующим API ключи.</p>
     *
     * @param city название города
     * @return ResponseEntity с данными о погоде
     */
    @GetMapping("/current")
    public Mono<ResponseEntity<WeatherDto>> getCurrentWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting weather for city: {}", city);

        return weatherService.getCurrentWeather(city)
            .map(weather -> {
                log.info("REST: Found weather for {}: {}°C",
                    city, weather.getMain().getTemp());
                return ResponseEntity.ok()
                    .header("X-Cache-Key", city.toLowerCase())
                    .body(weather);
            })
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает погоду по координатам.
     *
     * @param lat широта
     * @param lon долгота
     * @return ResponseEntity с данными о погоде
     */
    @GetMapping("/coordinates")
    public Mono<ResponseEntity<WeatherDto>> getWeatherByCoordinates(
        @RequestParam double lat,
        @RequestParam double lon) {
        log.info("REST: Getting weather by coordinates: lat={}, lon={}", lat, lon);

        return weatherService.getCurrentWeatherByCoordinates(lat, lon)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Получает детальную информацию о погоде.
     *
     * @param city название города
     * @return ResponseEntity с детальными данными о погоде
     */
    @GetMapping("/detailed")
    public Mono<ResponseEntity<WeatherDto>> getDetailedWeather(
        @RequestParam @NotNull String city) {
        log.info("REST: Getting detailed weather for city: {}", city);

        return weatherService.getDetailedWeather(city)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }

    /**
     * Проверяет доступность weather сервиса.
     *
     * @return ResponseEntity с статусом доступности
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkWeatherServiceHealth() {
        log.info("REST: Checking weather service health");

        return weatherService.isWeatherServiceAvailable()
            .map(available -> {
                Map<String, Object> status = Map.of(
                    "service", "weather-api",
                    "available", available,
                    "timestamp", System.currentTimeMillis()
                );

                HttpStatus httpStatus = available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                return ResponseEntity.status(httpStatus).body(status);
            });
    }
}
