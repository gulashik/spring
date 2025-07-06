package org.gualsh.demo.httpe.client;

import org.gualsh.demo.httpe.dto.Post;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Расширенный клиент для демонстрации продвинутых возможностей.
 * <p>
 * Показывает дополнительные возможности @HttpExchange:
 * </p>
 * <ul>
 * <li>Кастомные headers</li>
 * <li>Различные content types</li>
 * <li>Обработку ошибок</li>
 * <li>Сложные query parameters</li>
 * </ul>
 */
@HttpExchange("/")
public interface AdvancedHttpExchangeClient {

    /**
     * Демонстрирует работу с кастомными headers.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * В реальных приложениях headers часто добавляются
     * через ExchangeFilterFunction в WebClient конфигурации.
     * Здесь показан принцип работы.
     * </p>
     *
     * <pre>{@code
     * // Этот пример показывает концепцию
     * // В реальности headers добавляются в WebClient конфигурации
     * @GetExchange("/protected-resource")
     * Mono<String> getProtectedResource();
     * }</pre>
     *
     * @return защищенный ресурс
     */
    @GetExchange("/posts/1")
    Mono<Post> getPostWithHeaders();

    /**
     * Демонстрирует работу с формами.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Хотя @HttpExchange в основном работает с JSON,
     * можно настроить работу с form data через WebClient.
     * </p>
     *
     * <pre>{@code
     * // Для form data обычно используется MultiValueMap
     * MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
     * formData.add("key", "value");
     *
     * // Или настройка content-type в WebClient
     * }</pre>
     *
     * @return результат отправки формы
     */
    @PostExchange("/form-endpoint")
    Mono<String> submitForm();

    /**
     * Демонстрирует сложные query parameters.
     *
     * <h4>Образовательный момент</h4>
     * <p>
     * Показывает работу с множественными параметрами
     * и их опциональностью.
     * </p>
     *
     * <pre>{@code
     * // URL: /search?q=spring&category=tech&limit=10
     * Flux<Post> results = client.searchPosts("spring", "tech", 10, 0);
     * }</pre>
     *
     * @param query поисковый запрос
     * @param category категория
     * @param limit лимит результатов
     * @param offset смещение
     * @return результаты поиска
     */
    @GetExchange("/posts")
    Flux<Post> searchPosts(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Integer offset
    );
}

