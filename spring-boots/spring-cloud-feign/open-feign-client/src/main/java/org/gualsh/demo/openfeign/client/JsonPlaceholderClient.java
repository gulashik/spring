package org.gualsh.demo.openfeign.client;

import org.gualsh.demo.openfeign.dto.request.CreatePostRequest;
import org.gualsh.demo.openfeign.dto.response.Post;
import org.gualsh.demo.openfeign.dto.response.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Feign клиент для работы с JSONPlaceholder API.
 *
 * <p>
 * JSONPlaceholder (jsonplaceholder.typicode.com) - это бесплатный REST API для тестирования и прототипирования.
 * </p>
 *
 * <h3>Возможные возвращаемые типы:</h3>
 * <ul>
 * <li>В интерфейсе @FeignClient как возвращаемые типы можно использовать почти ллюбые типы,
 *  кроме Optional(дополнительная настройка), реактивные типы(Mono, Flux)</li>
 * </ul>
 *
 * <h3>Ключевые опции аннотации @FeignClient:</h3>
 * <ul>
 *   <li><strong>name</strong> - уникальное имя клиента в Spring контексте</li>
 *   <li><strong>url</strong> - базовый URL API (можно переопределить в конфигурации)</li>
 *   <li><strong>configuration</strong> - специфичная конфигурация для этого клиента</li>
 *   <li><strong>fallback</strong> - класс для обработки ошибок (требует Spring Cloud Circuit Breaker)</li>
 * </ul>
 *
 * <h3>Best Practices для @FeignClient:</h3>
 * <ul>
 *   <li>Используйте осмысленные имена для клиентов</li>
 *   <li>Выносите URL в конфигурацию для разных сред</li>
 *   <li>Группируйте методы по функциональности</li>
 *   <li>Документируйте назначение каждого endpoint</li>
 * </ul>
 *
 * <h3>Подводные камни:</h3>
 * <ul>
 *   <li>Имя клиента должно быть уникальным в Spring контексте</li>
 *   <li>URL должен быть доступен на момент старта приложения</li>
 *   <li>Методы должны использовать Spring Web аннотации</li>
 *   <li>Возвращаемые типы должны быть сериализуемыми</li>
 * </ul>
 *
 * <h3>Пример использования клиента:</h3>
 * <pre>{@code
 * @Service
 * public class PostService {
 *     private final JsonPlaceholderClient client;
 *
 *     public PostService(JsonPlaceholderClient client) {
 *         this.client = client;
 *     }
 *
 *     public List<Post> getUserPosts(Long userId) {
 *         return client.getPostsByUserId(userId);
 *     }
 * }
 * }</pre>
 */
@FeignClient(
    name = "jsonplaceholder",
    url = "${spring.cloud.openfeign.client.config.jsonplaceholder.url:https://jsonplaceholder.typicode.com}"
)
/*@FeignClient(
    name = "unique-client-name",           // Уникальное имя клиента
    url = "https://api.example.com",       // Базовый URL
    path = "/api/v1",                      // Общий путь для всех методов
    configuration = CustomConfig.class,    // Кастомная конфигурация
    fallback = MyServiceFallback.class,    // Fallback класс для Circuit Breaker
    fallbackFactory = MyFallbackFactory.class, // Factory для fallback
    primary = false                        // Не делать primary бином
)*/
public interface JsonPlaceholderClient {

    /**
     * Получает список всех постов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @GetMapping без параметров демонстрирует простейший случай GET запроса.
     * Feign автоматически десериализует JSON массив в List&lt;Post&gt;.
     * </p>
     *
     * <h3>Особенности работы со списками:</h3>
     * <ul>
     *   <li>Jackson автоматически обрабатывает JSON массивы</li>
     *   <li>Тип элементов списка определяется через Generic</li>
     *   <li>Пустой массив преобразуется в пустой List</li>
     *   <li>null ответ может привести к NullPointerException</li>
     * </ul>
     *
     * <h3>Производительность:</h3>
     * <p>
     * В production рекомендуется использовать пагинацию для больших наборов данных.
     * </p>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * List<Post> allPosts = client.getAllPosts();
     * System.out.println("Получено постов: " + allPosts.size());
     * 
     * // Обработка постов
     * allPosts.forEach(post -> {
     *     System.out.println("Пост ID: " + post.getId() + ", Заголовок: " + post.getTitle());
     * });
     * }</pre>
     *
     * @return список всех постов из JSONPlaceholder API
     */
    @GetMapping("/posts")
    List<Post> getAllPosts();

    /**
     * Получает посты с пагинацией.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует использование множественных @RequestParam для пагинации.
     * Параметры _start и _limit поддерживаются JSONPlaceholder API.
     * </p>
     *
     * <h3>Параметры пагинации JSONPlaceholder:</h3>
     * <ul>
     *   <li><strong>_start</strong> - смещение (offset), с какого элемента начинать</li>
     *   <li><strong>_limit</strong> - количество элементов на странице</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * // Первая страница, 10 элементов
     * List<Post> firstPage = client.getPostsWithPagination(0, 10);
     *
     * // Вторая страница, 10 элементов
     * List<Post> secondPage = client.getPostsWithPagination(10, 10);
     * 
     * // Постраничная загрузка
     * int pageSize = 5;
     * int page = 0;
     * List<Post> posts;
     * do {
     *     posts = client.getPostsWithPagination(page * pageSize, pageSize);
     *     posts.forEach(post -> System.out.println(post.getTitle()));
     *     page++;
     * } while (!posts.isEmpty());
     * }</pre>
     *
     * @param start смещение (номер первого элемента)
     * @param limit количество элементов на странице
     * @return список постов для указанной страницы
     */
    @GetMapping(value = "/posts",params = { "_start", "_limit"})
    List<Post> getPostsWithPagination(@RequestParam("_start") Integer start,
                                      @RequestParam("_limit") Integer limit);

    /**
     * Получает конкретный пост по ID.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @PathVariable демонстрирует передачу параметров через URL path.
     * Имя переменной в URL должно совпадать с именем параметра метода
     * или использовать value в аннотации.
     * </p>
     *
     * <h3>Обработка ошибок:</h3>
     * <ul>
     *   <li>404 Not Found - если пост с таким ID не существует</li>
     *   <li>400 Bad Request - если ID некорректный</li>
     *   <li>Feign выбросит FeignException для не-2xx ответов</li>
     * </ul>
     *
     * <h3>Валидация параметров:</h3>
     * <p>
     * В production добавьте валидацию: @PathVariable @Positive Long id
     * </p>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * try {
     *     Post post = client.getPostById(1L);
     *     System.out.println("Заголовок: " + post.getTitle());
     *     System.out.println("Содержимое: " + post.getBody());
     * } catch (FeignException.NotFound e) {
     *     System.err.println("Пост не найден");
     * } catch (FeignException e) {
     *     System.err.println("Ошибка API: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param id уникальный идентификатор поста
     * @return пост с указанным ID
     * @throws org.springframework.web.server.ResponseStatusException если пост не найден
     */
    @GetMapping("/posts/{id}")
    Post getPostById(@PathVariable("id") Long id);

    /**
     * Получает посты конкретного пользователя.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @RequestParam демонстрирует передачу query параметров.
     * URL будет выглядеть как: /posts?userId=1
     * </p>
     *
     * <h3>Особенности @RequestParam:</h3>
     * <ul>
     *   <li><strong>value</strong> - имя параметра в URL</li>
     *   <li><strong>required</strong> - обязательность параметра (по умолчанию true)</li>
     *   <li><strong>defaultValue</strong> - значение по умолчанию</li>
     * </ul>
     *
     * <h3>Best Practices:</h3>
     * <ul>
     *   <li>Используйте осмысленные имена параметров</li>
     *   <li>Документируйте допустимые значения</li>
     *   <li>Валидируйте входные параметры</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * Long userId = 1L;
     * List<Post> userPosts = client.getPostsByUserId(userId);
     * 
     * if (userPosts.isEmpty()) {
     *     System.out.println("У пользователя нет постов");
     * } else {
     *     System.out.println("Посты пользователя " + userId + ":");
     *     userPosts.forEach(post -> 
     *         System.out.println("- " + post.getTitle())
     *     );
     * }
     * }</pre>
     *
     * @param userId идентификатор пользователя
     * @return список постов указанного пользователя
     */
    @GetMapping("/posts")
    List<Post> getPostsByUserId(@RequestParam("userId") Long userId);

    /**
     * Создает новый пост.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * @PostMapping с @RequestBody демонстрирует отправку JSON данных.
     * Feign автоматически сериализует объект в JSON и устанавливает
     * Content-Type: application/json.
     * </p>
     *
     * <h3>Особенности POST запросов:</h3>
     * <ul>
     *   <li>Тело запроса сериализуется в JSON через Jackson</li>
     *   <li>Content-Type автоматически устанавливается</li>
     *   <li>@Valid активирует валидацию перед сериализацией</li>
     *   <li>Ответ десериализуется в указанный тип</li>
     * </ul>
     *
     * <h3>JSONPlaceholder поведение:</h3>
     * <p>
     * JSONPlaceholder не сохраняет данные, но возвращает корректный
     * ответ с сгенерированным ID для тестирования.
     * </p>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * CreatePostRequest request = new CreatePostRequest();
     * request.setTitle("Новый пост");
     * request.setBody("Содержимое нового поста");
     * request.setUserId(1L);
     * 
     * try {
     *     Post createdPost = client.createPost(request);
     *     System.out.println("Создан пост с ID: " + createdPost.getId());
     *     System.out.println("Заголовок: " + createdPost.getTitle());
     * } catch (ConstraintViolationException e) {
     *     System.err.println("Ошибка валидации: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param request данные для создания поста
     * @return созданный пост с сгенерированным ID
     * @throws jakarta.validation.ConstraintViolationException если данные невалидны
     */
    @PostMapping("/posts")
    Post createPost(@RequestBody @Valid CreatePostRequest request);

    /**
     * Обновляет существующий пост полностью (PUT).
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * PUT запрос предназначен для полного обновления ресурса.
     * Комбинирует @PathVariable для ID и @RequestBody для данных.
     * </p>
     *
     * <h3>Семантика PUT:</h3>
     * <ul>
     *   <li>Идемпотентная операция - повторные вызовы дают тот же результат</li>
     *   <li>Полностью заменяет ресурс</li>
     *   <li>Все поля должны быть указаны</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * Long postId = 1L;
     * CreatePostRequest updateRequest = new CreatePostRequest();
     * updateRequest.setTitle("Обновленный заголовок");
     * updateRequest.setBody("Обновленное содержимое");
     * updateRequest.setUserId(1L);
     * 
     * Post updatedPost = client.updatePost(postId, updateRequest);
     * System.out.println("Пост обновлен: " + updatedPost.getTitle());
     * }</pre>
     *
     * @param id ID обновляемого поста
     * @param request новые данные поста
     * @return обновленный пост
     */
    @PutMapping("/posts/{id}")
    Post updatePost(@PathVariable("id") Long id, @RequestBody @Valid CreatePostRequest request);

    /**
     * Частично обновляет пост (PATCH).
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * PATCH используется для частичного обновления ресурса.
     * Отправляются только изменяемые поля.
     * </p>
     *
     * <h3>Отличия PATCH от PUT:</h3>
     * <ul>
     *   <li>PATCH - частичное обновление</li>
     *   <li>PUT - полная замена</li>
     *   <li>PATCH может быть не идемпотентным</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * Long postId = 1L;
     * CreatePostRequest patchRequest = new CreatePostRequest();
     * patchRequest.setTitle("Только новый заголовок");
     * // body и userId можно не указывать - они не изменятся
     * 
     * Post patchedPost = client.patchPost(postId, patchRequest);
     * System.out.println("Заголовок изменен на: " + patchedPost.getTitle());
     * }</pre>
     *
     * @param id ID обновляемого поста
     * @param request частичные данные для обновления
     * @return обновленный пост
     */
    @PatchMapping("/posts/{id}")
    Post patchPost(@PathVariable("id") Long id, @RequestBody CreatePostRequest request);

    /**
     * Удаляет пост по ID.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * DELETE запрос для удаления ресурса. Обратите внимание на
     * возвращаемый тип Void - JSONPlaceholder не возвращает данные
     * при удалении.
     * </p>
     *
     * <h3>Особенности DELETE:</h3>
     * <ul>
     *   <li>Идемпотентная операция</li>
     *   <li>Может возвращать 204 No Content</li>
     *   <li>Повторное удаление может возвращать 404</li>
     * </ul>
     *
     * <h3>Обработка ответа:</h3>
     * <p>
     * Для операций без возвращаемых данных используйте:
     * </p>
     * <ul>
     *   <li>Void - для отсутствия данных</li>
     *   <li>ResponseEntity&lt;Void&gt; - для доступа к статус коду</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * Long postId = 1L;
     * try {
     *     client.deletePost(postId);
     *     System.out.println("Пост успешно удален");
     * } catch (FeignException.NotFound e) {
     *     System.err.println("Пост уже не существует");
     * } catch (FeignException e) {
     *     System.err.println("Ошибка при удалении: " + e.getMessage());
     * }
     * }</pre>
     *
     * @param id ID удаляемого поста
     */
    @DeleteMapping("/posts/{id}")
    void deletePost(@PathVariable("id") Long id);

    /**
     * Получает список всех пользователей.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Этот метод демонстрирует работу со сложными вложенными объектами.
     * User содержит nested объекты Address и Company, которые Jackson
     * автоматически десериализует.
     * </p>
     *
     * <h3>Вложенные объекты:</h3>
     * <ul>
     *   <li>Jackson рекурсивно обрабатывает вложенность</li>
     *   <li>Каждый уровень может иметь свои валидационные правила</li>
     *   <li>Отсутствующие поля заполняются null (если не настроено иначе)</li>
     * </ul>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * List<User> users = client.getAllUsers();
     * System.out.println("Найдено пользователей: " + users.size());
     * 
     * users.forEach(user -> {
     *     System.out.println("Пользователь: " + user.getName());
     *     System.out.println("Email: " + user.getEmail());
     *     if (user.getAddress() != null) {
     *         System.out.println("Город: " + user.getAddress().getCity());
     *     }
     *     if (user.getCompany() != null) {
     *         System.out.println("Компания: " + user.getCompany().getName());
     *     }
     * });
     * }</pre>
     *
     * @return список всех пользователей с полной информацией
     */
    @GetMapping("/users")
    List<User> getAllUsers();

    /**
     * Получает конкретного пользователя по ID.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Аналогично getPostById, но для пользователей. Демонстрирует
     * консистентность API design в рамках одного клиента.
     * </p>
     *
     * <h3>Пример использования:</h3>
     * <pre>{@code
     * try {
     *     User user = client.getUserById(1L);
     *     System.out.println("Имя пользователя: " + user.getName());
     *     System.out.println("Username: " + user.getUsername());
     *     System.out.println("Email: " + user.getEmail());
     *     System.out.println("Телефон: " + user.getPhone());
     *     System.out.println("Веб-сайт: " + user.getWebsite());
     * } catch (FeignException.NotFound e) {
     *     System.err.println("Пользователь не найден");
     * }
     * }</pre>
     *
     * @param id уникальный идентификатор пользователя
     * @return пользователь с указанным ID
     */
    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}
