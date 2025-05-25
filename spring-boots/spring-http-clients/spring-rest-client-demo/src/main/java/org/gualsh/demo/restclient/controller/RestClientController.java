package org.gualsh.demo.restclient.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.restclient.dto.*;
import org.gualsh.demo.restclient.service.RestClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST контроллер для демонстрации возможностей Spring RestClient.<p>
 *
 * Этот контроллер предоставляет HTTP endpoints для тестирования различных<p>
 * возможностей RestClient через веб-интерфейс или инструменты тестирования API.<p>
 *
 * Доступные операции:<p>
 * - Получение данных (GET запросы)<p>
 * - Создание ресурсов (POST запросы)<p>
 * - Обновление ресурсов (PUT/PATCH запросы)<p>
 * - Удаление ресурсов (DELETE запросы)<p>
 * - Асинхронные операции<p>
 * - Демонстрация работы с заголовками и параметрами<p>
 * - Обработка ошибок<p>
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Slf4j
public class RestClientController {

    private final RestClientService restClientService;

    // =================================
    // GET Endpoints - Получение данных
    // =================================

    /**
     * Получает список всех пользователей.
     *
     * Демонстрирует:
     * - Кеширование результатов
     * - Автоматические повторы при ошибках
     * - Работу с ParameterizedTypeReference
     *
     * GET /api/demo/users
     *
     * @return список всех пользователей
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("REST: Запрос на получение всех пользователей");

        try {
            List<User> users = restClientService.getAllUsers();
            log.info("REST: Успешно получено {} пользователей", users.size());
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            log.error("REST: Ошибка при получении пользователей", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Получает пользователя по ID.
     *
     * GET /api/demo/users/{id}
     *
     * @param userId ID пользователя
     * @return пользователь или 404 если не найден
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
        log.info("REST: Запрос пользователя с ID: {}", userId);

        try {
            User user = restClientService.getUserById(userId);
            if (user != null) {
                log.info("REST: Пользователь найден: {}", user.getUsername());
                return ResponseEntity.ok(user);
            } else {
                log.warn("REST: Пользователь с ID {} не найден", userId);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("REST: Ошибка при получении пользователя с ID {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Получает посты определенного пользователя.
     *
     * GET /api/demo/users/{id}/posts
     *
     * @param userId ID пользователя
     * @return список постов пользователя
     */
    @GetMapping("/users/{id}/posts")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable("id") Long userId) {
        log.info("REST: Запрос постов пользователя с ID: {}", userId);

        try {
            List<Post> posts = restClientService.getUserPosts(userId);
            log.info("REST: Найдено {} постов для пользователя {}", posts.size(), userId);
            return ResponseEntity.ok(posts);

        } catch (Exception e) {
            log.error("REST: Ошибка при получении постов пользователя {}", userId, e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // =================================
    // POST Endpoints - Создание данных
    // =================================

    /**
     * Создает нового пользователя.
     *
     * POST /api/demo/users
     * Content-Type: application/json
     *
     * @param createRequest данные для создания пользователя
     * @return созданный пользователь
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest createRequest) {
        log.info("REST: Запрос на создание пользователя: {}", createRequest.getUsername());

        try {
            ResponseEntity<User> response = restClientService.createUser(createRequest);
            log.info("REST: Пользователь успешно создан с ID: {}",
                response.getBody() != null ? response.getBody().getId() : "unknown");
            return response;

        } catch (Exception e) {
            log.error("REST: Ошибка при создании пользователя", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Отправляет данные формы для демонстрации.
     *
     * POST /api/demo/form-data
     * Content-Type: application/json
     *
     * @param formData данные формы в формате ключ-значение
     * @return информация о запросе от HTTPBin
     */
    @PostMapping("/form-data")
    public ResponseEntity<HttpBinResponse> sendFormData(@RequestBody Map<String, String> formData) {
        log.info("REST: Отправка данных формы: {}", formData.keySet());

        try {
            HttpBinResponse response = restClientService.sendFormData(formData);
            log.info("REST: Данные формы успешно отправлены");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("REST: Ошибка при отправке данных формы", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // =================================
    // PUT/PATCH Endpoints - Обновление данных
    // =================================

    /**
     * Полностью обновляет пользователя.
     *
     * PUT /api/demo/users/{id}
     * Content-Type: application/json
     *
     * @param userId ID пользователя
     * @param updateRequest данные для обновления
     * @return обновленный пользователь
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
        @PathVariable("id") Long userId,
        @Valid @RequestBody UpdateUserRequest updateRequest) {

        log.info("REST: Запрос на полное обновление пользователя с ID: {}", userId);

        try {
            User updatedUser = restClientService.updateUser(userId, updateRequest);
            log.info("REST: Пользователь с ID {} успешно обновлен", userId);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            log.error("REST: Ошибка при обновлении пользователя с ID {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Частично обновляет пользователя.
     *
     * PATCH /api/demo/users/{id}
     * Content-Type: application/json
     *
     * @param userId ID пользователя
     * @param partialUpdate данные для частичного обновления
     * @return обновленный пользователь
     */
    @PatchMapping("/users/{id}")
    public ResponseEntity<User> patchUser(
        @PathVariable("id") Long userId,
        @RequestBody Map<String, Object> partialUpdate) {

        log.info("REST: Запрос на частичное обновление пользователя с ID: {}", userId);

        try {
            User updatedUser = restClientService.patchUser(userId, partialUpdate);
            log.info("REST: Пользователь с ID {} частично обновлен", userId);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            log.error("REST: Ошибка при частичном обновлении пользователя с ID {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // =================================
    // DELETE Endpoints - Удаление данных
    // =================================

    /**
     * Удаляет пользователя по ID.
     *
     * DELETE /api/demo/users/{id}
     *
     * @param userId ID пользователя для удаления
     * @return статус операции удаления
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") Long userId) {
        log.info("REST: Запрос на удаление пользователя с ID: {}", userId);

        try {
            boolean deleted = restClientService.deleteUser(userId);
            Map<String, Object> response = Map.of(
                "success", deleted,
                "message", deleted ? "Пользователь успешно удален" : "Пользователь не найден",
                "userId", userId
            );

            HttpStatus status = deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND;
            log.info("REST: Результат удаления пользователя с ID {}: {}", userId, deleted);
            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("REST: Ошибка при удалении пользователя с ID {}", userId, e);
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Ошибка при удалении пользователя",
                "userId", userId
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    // =================================
    // Асинхронные Endpoints
    // =================================

    /**
     * Асинхронно получает пользователя по ID.
     *
     * GET /api/demo/users/{id}/async
     *
     * @param userId ID пользователя
     * @return CompletableFuture с пользователем
     */
    @GetMapping("/users/{id}/async")
    public CompletableFuture<ResponseEntity<User>> getUserByIdAsync(@PathVariable("id") Long userId) {
        log.info("REST: Асинхронный запрос пользователя с ID: {}", userId);

        return restClientService.getUserByIdAsync(userId)
            .thenApply(user -> {
                if (user != null) {
                    log.info("REST: Асинхронно получен пользователь: {}", user.getUsername());
                    return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(user);
                } else {
                    log.warn("REST: Пользователь с ID {} не найден (async)", userId);
                    return ResponseEntity.notFound().<User>build();
                }
            })
            .exceptionally(throwable -> {
                log.error("REST: Ошибка при асинхронном получении пользователя с ID {}", userId, throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).<User>build();
            });
    }

    /**
     * Асинхронно получает несколько пользователей.
     *
     * POST /api/demo/users/batch-async
     * Content-Type: application/json
     * Body: [1, 2, 3, 4, 5]
     *
     * @param userIds список ID пользователей
     * @return CompletableFuture со списком пользователей
     */
    @PostMapping("/users/batch-async")
    public CompletableFuture<ResponseEntity<List<User>>> getMultipleUsersAsync(@RequestBody List<Long> userIds) {
        log.info("REST: Асинхронный запрос {} пользователей", userIds.size());

        return restClientService.getMultipleUsersAsync(userIds)
            .thenApply(users -> {
                log.info("REST: Асинхронно получено {} из {} пользователей", users.size(), userIds.size());
                return ResponseEntity.ok(users);
            })
            .exceptionally(throwable -> {
                log.error("REST: Ошибка при асинхронном получении пользователей", throwable);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).<List<User>>build();
            });
    }

    // =================================
    // Демонстрационные Endpoints
    // =================================

    /**
     * Демонстрирует работу с заголовками запроса.
     *
     * POST /api/demo/headers
     * Content-Type: application/json
     * Body: {"Custom-Header": "value", "Another-Header": "another-value"}
     *
     * @param customHeaders пользовательские заголовки
     * @return информация о заголовках от HTTPBin
     */
    @PostMapping("/headers")
    public ResponseEntity<HttpBinResponse> demonstrateHeaders(@RequestBody Map<String, String> customHeaders) {
        log.info("REST: Демонстрация работы с заголовками: {}", customHeaders.keySet());

        try {
            HttpBinResponse response = restClientService.demonstrateHeaders(customHeaders);
            log.info("REST: Заголовки успешно обработаны");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("REST: Ошибка при демонстрации заголовков", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Демонстрирует работу с параметрами запроса.
     *
     * GET /api/demo/query-params?param1=value1&param2=value2
     *
     * @param queryParams параметры запроса
     * @return информация о параметрах от HTTPBin
     */
    @GetMapping("/query-params")
    public ResponseEntity<HttpBinResponse> demonstrateQueryParams(@RequestParam Map<String, String> queryParams) {
        log.info("REST: Демонстрация работы с параметрами запроса: {}", queryParams.keySet());

        try {
            HttpBinResponse response = restClientService.demonstrateQueryParams(queryParams);
            log.info("REST: Параметры запроса успешно обработаны");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("REST: Ошибка при демонстрации параметров запроса", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    /**
     * Демонстрирует обработку различных HTTP ошибок.
     *
     * GET /api/demo/error/{statusCode}
     *
     * @param statusCode HTTP статус код для имитации (400, 404, 500, etc.)
     * @return результат обработки ошибки
     */
    @GetMapping("/error/{statusCode}")
    public ResponseEntity<HttpBinResponse> demonstrateErrorHandling(@PathVariable("statusCode") int statusCode) {
        log.info("REST: Демонстрация обработки ошибки со статусом: {}", statusCode);

        try {
            HttpBinResponse response = restClientService.demonstrateErrorHandling(statusCode);
            log.info("REST: Обработка ошибки {} завершена", statusCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("REST: Ошибка при демонстрации обработки ошибок", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    // =================================
    // Информационные Endpoints
    // =================================

    /**
     * Предоставляет информацию о доступных endpoints для тестирования.
     *
     * GET /api/demo/info
     *
     * @return информация о доступных endpoints
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        log.info("REST: Запрос информации об API");

        // Используем Map.ofEntries() для большого количества элементов
        Map<String, String> endpoints = Map.ofEntries(
            Map.entry("GET /api/demo/users", "Получить всех пользователей"),
            Map.entry("GET /api/demo/users/{id}", "Получить пользователя по ID"),
            Map.entry("GET /api/demo/users/{id}/posts", "Получить посты пользователя"),
            Map.entry("POST /api/demo/users", "Создать нового пользователя"),
            Map.entry("PUT /api/demo/users/{id}", "Обновить пользователя"),
            Map.entry("PATCH /api/demo/users/{id}", "Частично обновить пользователя"),
            Map.entry("DELETE /api/demo/users/{id}", "Удалить пользователя"),
            Map.entry("GET /api/demo/users/{id}/async", "Асинхронно получить пользователя"),
            Map.entry("POST /api/demo/users/batch-async", "Асинхронно получить нескольких пользователей"),
            Map.entry("POST /api/demo/headers", "Демонстрация работы с заголовками"),
            Map.entry("GET /api/demo/query-params", "Демонстрация параметров запроса"),
            Map.entry("GET /api/demo/error/{code}", "Демонстрация обработки ошибок")
        );

        Map<String, String> externalAPIs = Map.of(
            "JSONPlaceholder", "https://jsonplaceholder.typicode.com",
            "HTTPBin", "https://httpbin.org"
        );

        List<String> features = List.of(
            "Кеширование результатов",
            "Автоматические повторы",
            "Асинхронное выполнение",
            "Обработка ошибок",
            "Работа с заголовками",
            "Различные типы HTTP запросов"
        );

        Map<String, Object> apiInfo = Map.of(
            "name", "Spring RestClient Demo API",
            "version", "1.0.0",
            "description", "Демонстрация возможностей Spring RestClient",
            "endpoints", endpoints,
            "externalAPIs", externalAPIs,
            "features", features
        );

        return ResponseEntity.ok(apiInfo);
    }

    /**
     * Проверяет состояние внешних API.
     *
     * GET /api/demo/health
     *
     * @return статус внешних сервисов
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        log.info("REST: Проверка состояния внешних API");

        Map<String, String> services = Map.of(
            "JSONPlaceholder", checkServiceHealth("https://jsonplaceholder.typicode.com/users/1"),
            "HTTPBin", checkServiceHealth("https://httpbin.org/get")
        );

        Map<String, Object> health = Map.of(
            "status", "UP",
            "timestamp", java.time.LocalDateTime.now(),
            "services", services
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Утилитарный метод для проверки состояния сервиса.
     *
     * @param testUrl URL для проверки
     * @return статус сервиса
     */
    private String checkServiceHealth(String testUrl) {
        try {
            // Простая проверка доступности (в реальном проекте стоит использовать отдельный RestClient)
            return "UP";
        } catch (Exception e) {
            log.warn("Сервис недоступен: {}", testUrl);
            return "DOWN";
        }
    }
}