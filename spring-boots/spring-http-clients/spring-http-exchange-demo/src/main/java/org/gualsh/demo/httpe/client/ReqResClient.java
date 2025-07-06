package org.gualsh.demo.httpe.client;

import org.gualsh.demo.httpe.dto.ReqResListResponse;
import org.gualsh.demo.httpe.dto.ReqResUser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * Клиент для работы с ReqRes API.
 *
 * <h3>Образовательный момент</h3>
 * <p>
 * Демонстрирует работу с другим API и дополнительные возможности:
 * </p>
 * <ul>
 * <li>Работу с paginated responses</li>
 * <li>Различные структуры данных</li>
 * <li>Query parameters для пагинации</li>
 * </ul>
 *
 * <h4>Пример использования</h4>
 * <pre>{@code
 * @Autowired
 * private ReqResClient client;
 *
 * // Получение первой страницы пользователей
 * Mono<ReqResListResponse<ReqResUser>> response = client.getUsers(1, 6);
 *
 * // Извлечение пользователей
 * Flux<ReqResUser> users = response.flatMapMany(r -> Flux.fromIterable(r.getData()));
 * }</pre>
 */
@HttpExchange("/api")
public interface ReqResClient {

    /**
     * Получает список пользователей с пагинацией.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Демонстрирует работу с paginated API.
     * Multiple @RequestParam для query parameters.
     * </p>
     *
     * <pre>{@code
     * // URL: /api/users?page=1&per_page=6
     * Mono<ReqResListResponse<ReqResUser>> response = client.getUsers(1, 6);
     *
     * // Получение данных
     * response.subscribe(r -> {
     *     System.out.println("Page: " + r.getPage());
     *     System.out.println("Total: " + r.getTotal());
     *     r.getData().forEach(user -> System.out.println(user.getEmail()));
     * });
     * }</pre>
     *
     * @param page номер страницы
     * @param perPage количество элементов на странице
     * @return ответ с пагинацией
     */
    @GetExchange("/users")
    Mono<ReqResListResponse<ReqResUser>> getUsers(
        @RequestParam(required = false) Integer page,
        @RequestParam(name = "per_page", required = false) Integer perPage
    );

    /**
     * Получает пользователя по ID.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Работа с wrapped response (data внутри объекта).
     * </p>
     *
     * @param id идентификатор пользователя
     * @return пользователь в wrapper объекте
     */
    @GetExchange("/users/{id}")
    Mono<ReqResListResponse<ReqResUser>> getUser(@PathVariable Long id);

    /**
     * Создает нового пользователя.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Создание ресурса с возвратом созданного объекта.
     * </p>
     *
     * @param user данные пользователя
     * @return созданный пользователь
     */
    @PostExchange("/users")
    Mono<ReqResUser> createUser(@RequestBody ReqResUser user);
}

