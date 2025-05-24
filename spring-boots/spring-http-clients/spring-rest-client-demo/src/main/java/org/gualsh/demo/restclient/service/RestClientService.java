package org.gualsh.demo.restclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.restclient.dto.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Основной сервис для демонстрации возможностей Spring RestClient.
 *
 * Этот сервис демонстрирует:
 * - Различные типы HTTP запросов (GET, POST, PUT, DELETE, PATCH)
 * - Работу с ParameterizedTypeReference для generic типов
 * - Кеширование результатов запросов
 * - Автоматические повторы при ошибках
 * - Асинхронное выполнение запросов
 * - Обработку различных типов ответов и ошибок
 * - Работу с заголовками и параметрами запросов
 *
 * @author Demo Author
 * @version 1.0.0
 */
@Service
@Slf4j
public class RestClientService {

    private final RestClient jsonPlaceholderClient;
    private final RestClient httpBinClient;
    private final RestClient genericClient;

    public RestClientService(
        @Qualifier("jsonPlaceholderRestClient") RestClient jsonPlaceholderClient,
        @Qualifier("httpBinRestClient") RestClient httpBinClient,
        @Qualifier("genericRestClient") RestClient genericClient) {

        this.jsonPlaceholderClient = jsonPlaceholderClient;
        this.httpBinClient = httpBinClient;
        this.genericClient = genericClient;
    }


    // =================================
    // GET Запросы с кешированием
    // =================================

    /**
     * Получает список всех пользователей с кешированием результата.
     *
     * Демонстрирует:
     * - Использование ParameterizedTypeReference для List<User>
     * - Кеширование результата в кеше "users"
     * - Обработку ошибок с помощью onStatus()
     *
     * @return список пользователей
     * @throws RestClientException если запрос не удался
     */
    @Cacheable(value = "users", key = "'all-users'")
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public List<User> getAllUsers() {
        log.info("Выполнение запроса на получение всех пользователей");

        try {
            return jsonPlaceholderClient
                .get()
                .uri("/users")
                .header("X-Request-ID", generateRequestId())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    log.error("Клиентская ошибка при получении пользователей: {}", response.getStatusCode());
                    throw new RestClientResponseException(
                        "Ошибка клиента при получении пользователей",
                        response.getStatusCode(),
                        response.getStatusText(),
                        response.getHeaders(),
                        null,
                        null
                    );
                })
                .onStatus(status -> status.is5xxServerError(), (request, response) -> {
                    log.error("Серверная ошибка при получении пользователей: {}", response.getStatusCode());
                    throw new RestClientResponseException(
                        "Серверная ошибка при получении пользователей",
                        response.getStatusCode(),
                        response.getStatusText(),
                        response.getHeaders(),
                        null,
                        null
                    );
                })
                .body(new ParameterizedTypeReference<List<User>>() {});

        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей", e);
            throw e;
        }
    }

    /**
     * Получает пользователя по ID с кешированием.
     *
     * @param userId ID пользователя
     * @return пользователь или null если не найден
     */
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        log.info("Получение пользователя с ID: {}", userId);

        try {
            return jsonPlaceholderClient
                .get()
                .uri("/users/{id}", userId)
                .header("X-Request-ID", generateRequestId())
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND), (request, response) -> {
                    log.warn("Пользователь с ID {} не найден", userId);
                    // Не выбрасываем исключение для 404, возвращаем null
                })
                .body(User.class);

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Пользователь с ID {} не найден", userId);
                return null;
            }
            log.error("Ошибка при получении пользователя с ID {}", userId, e);
            throw e;
        }
    }

    /**
     * Получает посты пользователя с использованием параметров запроса.
     *
     * @param userId ID пользователя
     * @return список постов пользователя
     */
    @Cacheable(value = "posts", key = "#userId")
    public List<Post> getUserPosts(Long userId) {
        log.info("Получение постов пользователя с ID: {}", userId);

        return jsonPlaceholderClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/posts")
                .queryParam("userId", userId)
                .build())
            .header("X-Request-ID", generateRequestId())
            .retrieve()
            .body(new ParameterizedTypeReference<List<Post>>() {});
    }

    // =================================
    // POST Запросы
    // =================================

    /**
     * Создает нового пользователя.
     *
     * Демонстрирует:
     * - POST запрос с JSON телом
     * - Обработку различных HTTP статусов
     * - Возврат полного ResponseEntity для доступа к заголовкам
     *
     * @param createRequest данные для создания пользователя
     * @return созданный пользователь с метаданными ответа
     */
    public ResponseEntity<User> createUser(CreateUserRequest createRequest) {
        log.info("Создание нового пользователя: {}", createRequest.getUsername());

        try {
            return jsonPlaceholderClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", generateRequestId())
                .body(createRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    log.error("Ошибка валидации при создании пользователя: {}", response.getStatusCode());
                    throw new RestClientResponseException(
                        "Ошибка валидации при создании пользователя",
                        response.getStatusCode(),
                        response.getStatusText(),
                        response.getHeaders(),
                        null,
                        null
                    );
                })
                .toEntity(User.class);

        } catch (Exception e) {
            log.error("Ошибка при создании пользователя", e);
            throw e;
        }
    }

    /**
     * Отправляет данные формы на HTTPBin для демонстрации.
     *
     * @param formData данные формы
     * @return ответ от HTTPBin с информацией о запросе
     */
    public HttpBinResponse sendFormData(Map<String, String> formData) {
        log.info("Отправка данных формы на HTTPBin: {}", formData.keySet());

        return httpBinClient
            .post()
            .uri("/post")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-Request-ID", generateRequestId())
            .body(formData)
            .retrieve()
            .body(HttpBinResponse.class);
    }

    // =================================
    // PUT и PATCH Запросы
    // =================================

    /**
     * Полностью обновляет пользователя (PUT).
     *
     * @param userId ID пользователя для обновления
     * @param updateRequest данные для обновления
     * @return обновленный пользователь
     */
    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        log.info("Полное обновление пользователя с ID: {}", userId);

        return jsonPlaceholderClient
            .put()
            .uri("/users/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", generateRequestId())
            .body(updateRequest)
            .retrieve()
            .body(User.class);
    }

    /**
     * Частично обновляет пользователя (PATCH).
     *
     * @param userId ID пользователя для обновления
     * @param partialUpdate данные для частичного обновления
     * @return обновленный пользователь
     */
    public User patchUser(Long userId, Map<String, Object> partialUpdate) {
        log.info("Частичное обновление пользователя с ID: {}", userId);

        return jsonPlaceholderClient
            .patch()
            .uri("/users/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Request-ID", generateRequestId())
            .body(partialUpdate)
            .retrieve()
            .body(User.class);
    }

    // =================================
    // DELETE Запросы
    // =================================

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя для удаления
     * @return true если удаление прошло успешно
     */
    public boolean deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);

        try {
            ResponseEntity<Void> response = jsonPlaceholderClient
                .delete()
                .uri("/users/{id}", userId)
                .header("X-Request-ID", generateRequestId())
                .retrieve()
                .toBodilessEntity();

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Пользователь с ID {} {}удален", userId, success ? "" : "НЕ ");
            return success;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Пользователь с ID {} не найден для удаления", userId);
                return false;
            }
            log.error("Ошибка при удалении пользователя с ID {}", userId, e);
            throw e;
        }
    }

    // =================================
    // Асинхронные методы
    // =================================

    /**
     * Асинхронно получает пользователя по ID.
     *
     * @param userId ID пользователя
     * @return CompletableFuture с пользователем
     */
    @Async
    public CompletableFuture<User> getUserByIdAsync(Long userId) {
        log.info("Асинхронное получение пользователя с ID: {}", userId);

        try {
            User user = getUserById(userId);
            return CompletableFuture.completedFuture(user);
        } catch (Exception e) {
            log.error("Ошибка при асинхронном получении пользователя", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Асинхронно получает несколько пользователей параллельно.
     *
     * @param userIds список ID пользователей
     * @return CompletableFuture со списком пользователей
     */
    @Async
    public CompletableFuture<List<User>> getMultipleUsersAsync(List<Long> userIds) {
        log.info("Асинхронное получение {} пользователей", userIds.size());

        List<CompletableFuture<User>> futures = userIds.stream()
            .map(this::getUserByIdAsync)
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .filter(user -> user != null)
                .toList());
    }

    // =================================
    // Демонстрация работы с заголовками и параметрами
    // =================================

    /**
     * Демонстрирует работу с заголовками запроса.
     *
     * @param customHeaders дополнительные заголовки
     * @return информация о заголовках от HTTPBin
     */
    public HttpBinResponse demonstrateHeaders(Map<String, String> customHeaders) {
        log.info("Демонстрация работы с заголовками");

        RestClient.RequestHeadersSpec<?> request = httpBinClient
            .get()
            .uri("/headers")
            .header("X-Request-ID", generateRequestId())
            .header("X-Demo-Header", "RestClient-Demo")
            .header("X-Timestamp", LocalDateTime.now().toString());

        // Добавляем пользовательские заголовки
        customHeaders.forEach(request::header);

        return request
            .retrieve()
            .body(HttpBinResponse.class);
    }

    /**
     * Демонстрирует работу с параметрами запроса.
     *
     * @param queryParams параметры запроса
     * @return информация о параметрах от HTTPBin
     */
    public HttpBinResponse demonstrateQueryParams(Map<String, String> queryParams) {
        log.info("Демонстрация работы с параметрами запроса");

        return httpBinClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder.path("/get");
                queryParams.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            })
            .header("X-Request-ID", generateRequestId())
            .retrieve()
            .body(HttpBinResponse.class);
    }

    // =================================
    // Утилитарные методы
    // =================================

    /**
     * Генерирует уникальный ID для запроса.
     *
     * @return уникальный ID запроса
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Демонстрирует обработку различных типов ошибок.
     *
     * @param statusCode HTTP статус код для имитации
     * @return ответ с информацией об ошибке
     */
    public HttpBinResponse demonstrateErrorHandling(int statusCode) {
        log.info("Демонстрация обработки ошибки со статусом: {}", statusCode);

        try {
            return httpBinClient
                .get()
                .uri("/status/{code}", statusCode)
                .header("X-Request-ID", generateRequestId())
                .retrieve()
                .body(HttpBinResponse.class);

        } catch (RestClientResponseException e) {
            log.error("Получена ошибка HTTP {}: {}", e.getStatusCode(), e.getStatusText());

            // Создаем объект ошибки для демонстрации
            HttpBinResponse errorResponse = HttpBinResponse.builder()
                .url("/status/" + statusCode)
                .origin("error-simulation")
                .build();

            return errorResponse;
        }
    }
}