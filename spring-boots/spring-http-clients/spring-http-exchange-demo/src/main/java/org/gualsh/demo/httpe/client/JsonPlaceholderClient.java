package org.gualsh.demo.httpe.client;

import org.gualsh.demo.httpe.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Клиент для работы с JSONPlaceholder API.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Демонстрирует базовые возможности @HttpExchange:
 * </p>
 * <ul>
 * <li>@HttpExchange на уровне интерфейса для общих настроек</li>
 * <li>Различные HTTP методы через специализированные аннотации</li>
 * <li>Работу с path variables через @PathVariable</li>
 * <li>Query parameters через @RequestParam</li>
 * <li>Request body через @RequestBody</li>
 * <li>Реактивные типы возвращаемых данных</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * @Autowired
 * private JsonPlaceholderClient client;
 *
 * // Получение всех пользователей
 * Flux<User> users = client.getUsers();
 *
 * // Получение конкретного пользователя
 * Mono<User> user = client.getUser(1L);
 *
 * // Создание нового поста
 * Mono<Post> newPost = client.createPost(createRequest);
 * }</pre>
 *
 * <h4>Почему именно интерфейс</h4>
 * <p>
 * Использование интерфейса обеспечивает:
 * </p>
 * <ul>
 * <li>Декларативность - описываем что делаем, а не как</li>
 * <li>Testability - легко мокать для тестов</li>
 * <li>Separation of concerns - отделяем API от реализации</li>
 * <li>Spring автоматически создает прокси-реализацию</li>
 * </ul>
 */
@HttpExchange("/")
public interface JsonPlaceholderClient {

    /**
     * Получает список всех пользователей.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @GetExchange - специализированная аннотация для GET запросов.
     * Flux&lt;User&gt; означает поток пользователей (может быть много).
     * </p>
     *
     * <pre>{@code
     * // Использование
     * Flux<User> users = client.getUsers();
     *
     * // Подписка на результат
     * users.subscribe(user -> System.out.println(user.getName()));
     *
     * // Конвертация в List
     * List<User> userList = users.collectList().block();
     * }</pre>
     *
     * @return поток пользователей
     */
    @GetExchange("/users")
    Flux<User> getUsers();

    /**
     * Получает пользователя по ID.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @PathVariable связывает параметр метода с path variable в URL.
     * Mono&lt;User&gt; означает одно значение (может быть empty).
     * </p>
     *
     * <pre>{@code
     * // URL: /users/1
     * Mono<User> user = client.getUser(1L);
     *
     * // Обработка результата
     * user.subscribe(
     *     u -> System.out.println("Found: " + u.getName()),
     *     error -> System.err.println("Error: " + error.getMessage()),
     *     () -> System.out.println("User not found")
     * );
     * }</pre>
     *
     * @param id идентификатор пользователя
     * @return пользователь или empty
     */
    @GetExchange("/users/{id}")
    Mono<User> getUser(@PathVariable Long id);

    /**
     * Получает список постов с опциональной фильтрацией.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @RequestParam для query parameters.
     * Параметр required = false делает параметр опциональным.
     * </p>
     *
     * <pre>{@code
     * // Все посты: /posts
     * Flux<Post> allPosts = client.getPosts(null);
     *
     * // Посты пользователя: /posts?userId=1
     * Flux<Post> userPosts = client.getPosts(1L);
     * }</pre>
     *
     * @param userId идентификатор пользователя (опционально)
     * @return поток постов
     */
    @GetExchange("/posts")
    Flux<Post> getPosts(@RequestParam(required = false) Long userId);

    /**
     * Получает пост по ID.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Простой пример получения одного ресурса по ID.
     * </p>
     *
     * @param id идентификатор поста
     * @return пост
     */
    @GetExchange("/posts/{id}")
    Mono<Post> getPost(@PathVariable Long id);

    /**
     * Получает комментарии к посту.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует работу с вложенными ресурсами.
     * URL: /posts/{postId}/comments
     * </p>
     *
     * <pre>{@code
     * // Получение комментариев к посту
     * Flux<Comment> comments = client.getPostComments(1L);
     *
     * // Подсчет количества комментариев
     * Mono<Long> count = comments.count();
     * }</pre>
     *
     * @param postId идентификатор поста
     * @return поток комментариев
     */
    @GetExchange("/posts/{postId}/comments")
    Flux<Comment> getPostComments(@PathVariable Long postId);

    /**
     * Создает новый пост.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @PostExchange для создания новых ресурсов.
     * @RequestBody для передачи данных в теле запроса.
     * </p>
     *
     * <pre>{@code
     * CreatePostRequest request = CreatePostRequest.builder()
     *     .userId(1L)
     *     .title("New Post")
     *     .body("Post content")
     *     .build();
     *
     * Mono<Post> newPost = client.createPost(request);
     * }</pre>
     *
     * @param request данные для создания поста
     * @return созданный пост
     */
    @PostExchange("/posts")
    Mono<Post> createPost(@RequestBody CreatePostRequest request);

    /**
     * Обновляет пост полностью.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @PutExchange для полного обновления ресурса.
     * PUT семантически означает замену всего ресурса.
     * </p>
     *
     * <pre>{@code
     * CreatePostRequest update = CreatePostRequest.builder()
     *     .userId(1L)
     *     .title("Updated Title")
     *     .body("Updated content")
     *     .build();
     *
     * Mono<Post> updatedPost = client.updatePost(1L, update);
     * }</pre>
     *
     * @param id идентификатор поста
     * @param request новые данные поста
     * @return обновленный пост
     */
    @PutExchange("/posts/{id}")
    Mono<Post> updatePost(@PathVariable Long id, @RequestBody CreatePostRequest request);

    /**
     * Частично обновляет пост.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @PatchExchange для частичного обновления.
     * PATCH семантически означает изменение части ресурса.
     * </p>
     *
     * <pre>{@code
     * UpdatePostRequest patch = UpdatePostRequest.builder()
     *     .title("New Title Only")
     *     .build();
     *
     * Mono<Post> patchedPost = client.patchPost(1L, patch);
     * }</pre>
     *
     * @param id идентификатор поста
     * @param request данные для обновления
     * @return обновленный пост
     */
    @PatchExchange("/posts/{id}")
    Mono<Post> patchPost(@PathVariable Long id, @RequestBody UpdatePostRequest request);

    /**
     * Удаляет пост.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * @DeleteExchange для удаления ресурсов.
     * Mono&lt;Void&gt; означает, что операция не возвращает данных.
     * </p>
     *
     * <pre>{@code
     * Mono<Void> deletion = client.deletePost(1L);
     *
     * // Ожидание завершения операции
     * deletion.block();
     *
     * // Или с callback
     * deletion.subscribe(
     *     null, // onNext не вызывается для Void
     *     error -> System.err.println("Delete failed: " + error),
     *     () -> System.out.println("Post deleted successfully")
     * );
     * }</pre>
     *
     * @param id идентификатор поста
     * @return пустой результат
     */
    @DeleteExchange("/posts/{id}")
    Mono<Void> deletePost(@PathVariable Long id);
}

