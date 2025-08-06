package org.gualsh.demo.curbreaker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.model.ApiResponse;
import org.gualsh.demo.curbreaker.service.ExternalApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST контроллер для демонстрации Spring Cloud Circuit Breaker.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Данный контроллер предоставляет API endpoints для тестирования различных
 * сценариев работы Circuit Breaker. Каждый endpoint демонстрирует определенные
 * аспекты использования паттерна в реальных приложениях.
 * </p>
 *
 * <p><strong>Основные принципы контроллера с Circuit Breaker:</strong></p>
 * <ul>
 *   <li>Контроллер не должен знать о Circuit Breaker - логика изолирована в сервисах</li>
 *   <li>Graceful handling ошибок с meaningful HTTP статусами</li>
 *   <li>Логирование для мониторинга и диагностики</li>
 *   <li>Валидация входных данных</li>
 * </ul>
 *
 * <p><strong>Endpoints для тестирования:</strong></p>
 * <ul>
 *   <li>GET /api/external/posts/{id} - тест внешнего API</li>
 *   <li>GET /api/users/{id} - тест базы данных</li>
 *   <li>POST /api/email/send - тест email сервиса</li>
 *   <li>GET /api/test/scenario/{scenario} - тест различных сценариев</li>
 * </ul>
 *
 * @see ExternalApiService
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {

    private final ExternalApiService externalApiService;

    /**
     * Получение поста из внешнего API (асинхронно).
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Endpoint демонстрирует работу с reactive типами и Circuit Breaker.
     * Возвращает Mono, что позволяет Spring WebFlux обрабатывать запрос асинхронно.
     * </p>
     *
     * @param id идентификатор поста
     * @return асинхронный ответ с данными поста
     */
    @GetMapping("/external/posts/{id}")
    public Mono<ResponseEntity<ApiResponse>> getPost(@PathVariable Long id) {
        log.info("Запрос поста с ID: {}", id);

        return externalApiService.getPostAsync(id)
            .map(post -> {
                if (post.getTitle().contains("недоступен")) {
                    // Fallback данные - возвращаем 503 Service Unavailable
                    return ResponseEntity.status(503).body(post);
                }
                return ResponseEntity.ok(post);
            })
            .doOnSuccess(response -> {
                log.debug("Ответ отправлен для поста {}: {}", id, response.getStatusCode());
            })
            .onErrorResume(throwable -> {
                log.error("Ошибка при получении поста {}: {}", id, throwable.getMessage());

                ApiResponse errorResponse = ApiResponse.builder()
                    .id(id)
                    .title("Ошибка сервиса")
                    .body("Произошла ошибка при обработке запроса: " + throwable.getMessage())
                    .userId(0L)
                    .build();

                return Mono.just(ResponseEntity.status(500).body(errorResponse));
            });
    }

    /**
     * Получение всех постов из внешнего API.
     *
     * @return поток всех постов
     */
    @GetMapping("/external/posts")
    public Flux<ApiResponse> getAllPosts() {
        log.info("Запрос всех постов");

        return externalApiService.getAllPostsAsync()
            .doOnComplete(() -> log.debug("Все посты отправлены"))
            .onErrorResume(throwable -> {
                log.error("Ошибка при получении всех постов: {}", throwable.getMessage());

                ApiResponse errorResponse = ApiResponse.builder()
                    .id(0L)
                    .title("Ошибка загрузки")
                    .body("Не удалось загрузить список постов")
                    .userId(0L)
                    .build();

                return Flux.just(errorResponse);
            });
    }


    /**
     * Тестирование различных сценариев Circuit Breaker.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Специальный endpoint для демонстрации работы Circuit Breaker в различных
     * ситуациях: успех, ошибка, timeout. Полезно для понимания поведения паттерна.
     * </p>
     *
     * @param scenario тип сценария: "success", "error", "timeout"
     * @return результат выполнения сценария
     */
    @GetMapping("/test/scenario/{scenario}")
    public Mono<ResponseEntity<Map<String, Object>>> testScenario(@PathVariable String scenario) {
        log.info("Тестирование сценария: {}", scenario);

        return externalApiService.testCircuitBreakerScenario(scenario)
            .map(result -> {
                Map<String, Object> response = Map.of(
                    "scenario", scenario,
                    "result", result,
                    "success", true
                );
                return ResponseEntity.ok(response);
            })
            .onErrorResume(throwable -> {
                log.error("Ошибка в сценарии {}: {}", scenario, throwable.getMessage());

                Map<String, Object> response = Map.of(
                    "scenario", scenario,
                    "result", "Ошибка: " + throwable.getMessage(),
                    "success", false
                );
                return Mono.just(ResponseEntity.status(500).body(response));
            });
    }
}