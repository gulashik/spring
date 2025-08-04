package org.gualsh.demo.curbreaker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.curbreaker.model.ApiResponse;
import org.gualsh.demo.curbreaker.model.EmailRequest;
import org.gualsh.demo.curbreaker.model.User;
import org.gualsh.demo.curbreaker.service.DatabaseService;
import org.gualsh.demo.curbreaker.service.EmailService;
import org.gualsh.demo.curbreaker.service.ExternalApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
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
 * @author Educational Demo
 * @see ExternalApiService
 * @see DatabaseService
 * @see EmailService
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {

    private final ExternalApiService externalApiService;
    private final DatabaseService databaseService;
    private final EmailService emailService;

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
     * Получение пользователя из базы данных.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Синхронный endpoint для демонстрации работы Circuit Breaker с
     * блокирующими операциями (например, JPA repositories).
     * </p>
     *
     * @param id идентификатор пользователя
     * @return пользователь или 404 если не найден
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        log.info("Запрос пользователя с ID: {}", id);

        try {
            User user = databaseService.findById(id);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (user.getName().contains("недоступен")) {
                // Fallback данные - возвращаем 503 Service Unavailable
                return ResponseEntity.status(503).body(user);
            }

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("Ошибка при получении пользователя {}: {}", id, e.getMessage());

            User errorUser = User.builder()
                .id(id)
                .name("Ошибка сервиса")
                .email("error@system.local")
                .additionalInfo("Произошла ошибка: " + e.getMessage())
                .build();

            return ResponseEntity.status(500).body(errorUser);
        }
    }

    /**
     * Получение всех пользователей.
     *
     * @return список всех пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Запрос всех пользователей");

        try {
            List<User> users = databaseService.findAll();
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            log.error("Ошибка при получении всех пользователей: {}", e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * Создание нового пользователя.
     *
     * @param user данные пользователя
     * @return созданный пользователь
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("Создание пользователя: {}", user.getName());

        try {
            User savedUser = databaseService.save(user);
            return ResponseEntity.status(201).body(savedUser);

        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Отправка email.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Endpoint для тестирования email сервиса с Circuit Breaker.
     * Демонстрирует правильную обработку ошибок отправки.
     * </p>
     *
     * @param emailRequest запрос на отправку email
     * @return результат отправки
     */
    @PostMapping("/email/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Отправка email: {} -> {}", emailRequest.getFrom(), emailRequest.getTo());

        try {
            boolean sent = emailService.sendEmail(emailRequest);

            Map<String, Object> response = Map.of(
                "success", sent,
                "message", sent ? "Email отправлен успешно" : "Email поставлен в очередь",
                "to", emailRequest.getTo()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Ошибка при отправке email: {}", e.getMessage());

            Map<String, Object> response = Map.of(
                "success", false,
                "message", "Ошибка отправки email: " + e.getMessage(),
                "to", emailRequest.getTo()
            );

            return ResponseEntity.status(500).body(response);
        }
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

    /**
     * Health check endpoint для мониторинга состояния сервисов.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Health check endpoint полезен для мониторинга состояния различных
     * сервисов через Circuit Breaker. Может использоваться load balancer'ами
     * и системами оркестрации.
     * </p>
     *
     * @return состояние всех сервисов
     */
    @GetMapping("/health/services")
    public ResponseEntity<Map<String, Object>> getServicesHealth() {
        log.debug("Проверка состояния сервисов");

        Map<String, Object> health = Map.of(
            "database", databaseService.isHealthy(),
            "email", emailService.isEmailServiceHealthy(),
            "timestamp", java.time.LocalDateTime.now().toString()
        );

        boolean allHealthy = health.values().stream()
            .filter(Boolean.class::isInstance)
            .map(Boolean.class::cast)
            .allMatch(Boolean::booleanValue);

        return ResponseEntity
            .status(allHealthy ? 200 : 503)
            .body(Map.of(
                "status", allHealthy ? "UP" : "DOWN",
                "services", health
            ));
    }

    /**
     * Информация о приложении.
     *
     * @return информация о приложении и версиях
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = Map.of(
            "application", "Spring Circuit Breaker Demo",
            "version", "1.0.0",
            "springBoot", "3.3.4",
            "resilience4j", "2.2.0",
            "description", "Образовательный проект для демонстрации Spring Cloud Circuit Breaker",
            "endpoints", Map.of(
                "external_api", "/api/external/posts/{id}",
                "database", "/api/users/{id}",
                "email", "/api/email/send",
                "test_scenarios", "/api/test/scenario/{scenario}",
                "health", "/api/health/services",
                "actuator", "/actuator"
            )
        );

        return ResponseEntity.ok(info);
    }
}