package org.gualsh.demo.webclient.client;


import org.gualsh.demo.webclient.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Декларативный HTTP клиент для JSONPlaceholder API с использованием @HttpExchange.
 *
 * <p>@HttpExchange представляет новый подход Spring 6.0+ для создания HTTP клиентов
 * в декларативном стиле, похожем на Spring Cloud OpenFeign, но с поддержкой
 * реактивных типов и встроенной интеграцией с WebClient.</p>
 *
 * <p>Преимущества @HttpExchange перед прямым использованием WebClient:</p>
 * <ul>
 *   <li>Декларативный стиль - меньше boilerplate кода</li>
 *   <li>Автоматическая сериализация/десериализация</li>
 *   <li>Встроенная поддержка валидации</li>
 *   <li>Легкое тестирование через мокирование интерфейса</li>
 *   <li>Консистентная обработка ошибок</li>
 * </ul>
 *
 * <p>Поддерживаемые аннотации:</p>
 * <ul>
 *   <li>@GetExchange - GET запросы</li>
 *   <li>@PostExchange - POST запросы</li>
 *   <li>@PutExchange - PUT запросы</li>
 *   <li>@PatchExchange - PATCH запросы</li>
 *   <li>@DeleteExchange - DELETE запросы</li>
 * </ul>
 *
 * @author Demo
 * @version 1.0
 * @see org.springframework.web.service.annotation.HttpExchange
 * @since Spring 6.0
 */
@HttpExchange(url = "/", accept = "application/json", contentType = "application/json")
public interface JsonPlaceholderClient {

    /**
     * Получает список всех пользователей.
     *
     * <p>Эквивалентно WebClient:</p>
     * <pre>{@code
     * webClient.get()
     *     .uri("/users")
     *     .retrieve()
     *     .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {});
     * }</pre>
     *
     * @return Mono со списком всех пользователей
     */
    @GetExchange("/users")
    Mono<List<UserDto>> getAllUsers();

    /**
     * Получает пользователя по ID.
     *
     * <p>Демонстрирует использование @PathVariable для подстановки в URL.</p>
     *
     * @param userId идентификатор пользователя
     * @return Mono с пользователем
     */
    @GetExchange("/users/{id}")
    Mono<UserDto> getUserById(@PathVariable("id") Long userId);

    /**
     * Получает посты пользователя с использованием query параметра.
     *
     * <p>Демонстрирует использование @RequestParam для query параметров.
     * Возвращает Flux для потоковой обработки постов.</p>
     *
     * @param userId идентификатор пользователя
     * @return Flux с постами пользователя
     */
    @GetExchange("/posts")
    Flux<PostDto> getPostsByUserId(@RequestParam("userId") Long userId);

    /**
     * Получает все посты с пагинацией.
     *
     * <p>Демонстрирует использование нескольких query параметров.</p>
     *
     * @param start начальная позиция (для пагинации)
     * @param limit максимальное количество элементов
     * @return Mono со списком постов
     */
    @GetExchange("/posts")
    Mono<List<PostDto>> getAllPostsPaginated(
        @RequestParam("_start") int start,
        @RequestParam("_limit") int limit
    );

    /**
     * Создает новый пост.
     *
     * <p>Демонстрирует использование @RequestBody для передачи JSON данных
     * и автоматическую валидацию с @Valid.</p>
     *
     * @param createPostDto данные для создания поста
     * @return Mono с созданным постом
     */
    @PostExchange("/posts")
    Mono<PostDto> createPost(@Valid @RequestBody CreatePostDto createPostDto);

    /**
     * Обновляет существующий пост полностью (PUT).
     *
     * <p>Демонстрирует комбинацию path variable и request body.</p>
     *
     * @param postId идентификатор поста
     * @param updatePostDto новые данные поста
     * @return Mono с обновленным постом
     */
    @PutExchange("/posts/{id}")
    Mono<PostDto> updatePost(
        @PathVariable("id") Long postId,
        @Valid @RequestBody UpdatePostDto updatePostDto
    );

    /**
     * Частично обновляет пост (PATCH).
     *
     * <p>Использует Map для динамических полей обновления.</p>
     *
     * @param postId идентификатор поста
     * @param updates Map с полями для обновления
     * @return Mono с обновленным постом
     */
    @PatchExchange("/posts/{id}")
    Mono<PostDto> patchPost(
        @PathVariable("id") Long postId,
        @RequestBody Map<String, Object> updates
    );

    /**
     * Удаляет пост.
     *
     * <p>Возвращает Mono&lt;Void&gt; для операций без возвращаемого значения.</p>
     *
     * @param postId идентификатор поста для удаления
     * @return Mono<Void> подтверждающий удаление
     */
    @DeleteExchange("/posts/{id}")
    Mono<Void> deletePost(@PathVariable("id") Long postId);

    /**
     * Получает комментарии к посту.
     *
     * <p>Демонстрирует вложенные ресурсы в URL.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    @GetExchange("/posts/{postId}/comments")
    Flux<CommentDto> getCommentsByPostId(@PathVariable Long postId);

    /**
     * Получает все комментарии с фильтрацией по посту.
     *
     * <p>Альтернативный способ получения комментариев через query параметр.</p>
     *
     * @param postId идентификатор поста
     * @return Flux с комментариями
     */
    @GetExchange("/comments")
    Flux<CommentDto> getCommentsByPostIdQuery(@RequestParam("postId") Long postId);

    /**
     * Получает пост по ID.
     *
     * <p>Простой GET запрос для получения конкретного поста.</p>
     *
     * @param postId идентификатор поста
     * @return Mono с постом
     */
    @GetExchange("/posts/{id}")
    Mono<PostDto> getPostById(@PathVariable("id") Long postId);

    /**
     * Получает все посты без параметров.
     *
     * <p>Базовый GET запрос без параметров.</p>
     *
     * @return Mono со списком всех постов
     */
    @GetExchange("/posts")
    Mono<List<PostDto>> getAllPosts();
}