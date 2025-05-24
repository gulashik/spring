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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Основной сервис для демонстрации возможностей Spring RestClient.
 * <p>
 * Этот сервис демонстрирует:
 * <ul>
 *     <li>Различные типы HTTP запросов (GET, POST, PUT, DELETE, PATCH)</li>
 *     <li>Работу с ParameterizedTypeReference для generic типов</li>
 *     <li>Кеширование результатов запросов</li>
 *     <li>Автоматические повторы при ошибках</li>
 *     <li>Асинхронное выполнение запросов</li>
 *     <li>Обработку различных типов ответов и ошибок</li>
 *     <li>Работу с заголовками и параметрами запросов</li>
 * </ul>
 * Сервис взаимодействует с несколькими внешними API:
 * <ul>
 *     <li>JSONPlaceholder - для демонстрации CRUD операций</li>
 *     <li>HTTPBin - для демонстрации различных параметров запросов</li>
 * </ul>
 *
 * @see RestClient
 * @see org.gualsh.demo.restclient.controller.RestClientController
 */
@Service
@Slf4j
public class RestClientService {

    /**
     * Префикс для идентификаторов запросов
     */
    private static final String REQUEST_ID_PREFIX = "REQ-";
    
    /**
     * Имя заголовка для идентификатора запроса
     */
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    /**
     * Название кеша для пользователей
     */
    private static final String USERS_CACHE = "users";
    
    /**
     * Название кеша для постов
     */
    private static final String POSTS_CACHE = "posts";
    
    /**
     * Ключ кеша для всех пользователей
     */
    private static final String ALL_USERS_CACHE_KEY = "'all-users'";
    
    /**
     * Маршрут для запросов пользователей
     */
    private static final String USERS_PATH = "/users";
    
    /**
     * Маршрут для запросов постов
     */
    private static final String POSTS_PATH = "/posts";

    /**
     * RestClient для работы с JSONPlaceholder API.
     * Используется для операций с пользователями и постами.
     */
    private final RestClient jsonPlaceholderClient;
    
    /**
     * RestClient для работы с HTTPBin API.
     * Используется для демонстрации различных параметров HTTP запросов.
     */
    private final RestClient httpBinClient;
    
    /**
     * Общий RestClient для запросов к произвольным URL.
     */
    private final RestClient genericClient;

    /**
     * Создает экземпляр сервиса с настроенными RestClient'ами.
     *
     * @param jsonPlaceholderClient RestClient для JSONPlaceholder API (инжектируется через @Qualifier)
     * @param httpBinClient RestClient для HTTPBin API (инжектируется через @Qualifier)
     * @param genericClient Общий RestClient для других запросов (инжектируется через @Qualifier)
     */
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
     * <ul>
     *     <li>Использование ParameterizedTypeReference для List&lt;User&gt;</li>
     *     <li>Кеширование результата в кеше "users"</li>
     *     <li>Обработку ошибок с помощью onStatus()</li>
     *     <li>Автоматические повторы запросов при ошибках</li>
     * </ul>
     *
     * @return список пользователей или пустой список при ошибке
     * @throws RestClientException если запрос не удался после всех попыток повтора
     */
    @Cacheable(value = USERS_CACHE, key = ALL_USERS_CACHE_KEY)
    @Retryable(
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public List<User> getAllUsers() {
        log.info("Выполнение запроса на получение всех пользователей");
        String requestId = generateRequestId();

        try {
            List<User> users = jsonPlaceholderClient
                .get()
                .uri(USERS_PATH)
                .header(REQUEST_ID_HEADER, requestId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, response) -> {
                    log.error("Клиентская ошибка при получении пользователей: {} (RequestId: {})", 
                        response.getStatusCode(), requestId);
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
                    log.error("Серверная ошибка при получении пользователей: {} (RequestId: {})", 
                        response.getStatusCode(), requestId);
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

            log.info("Успешно получено {} пользователей (RequestId: {})", 
                users != null ? users.size() : 0, requestId);
            return users;

        } catch (Exception e) {
            log.error("Ошибка при получении списка пользователей (RequestId: {})", requestId, e);
            throw e;
        }
    }

    /**
     * Получает пользователя по ID с кешированием.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Кеширование результата по динамическому ключу</li>
     *     <li>Обработку ошибки 404 (NOT_FOUND) без исключения</li>
     *     <li>Добавление идентификатора запроса в заголовки</li>
     * </ul>
     *
     * @param userId ID пользователя для поиска
     * @return объект пользователя или null если пользователь не найден
     * @throws RestClientResponseException если произошла ошибка при обращении к API (кроме 404)
     */
    @Cacheable(value = USERS_CACHE, key = "#userId")
    public User getUserById(Long userId) {
        log.info("Получение пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            User user = jsonPlaceholderClient
                .get()
                .uri(USERS_PATH + "/{id}", userId)
                .header(REQUEST_ID_HEADER, requestId)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND), (request, response) -> {
                    log.warn("Пользователь с ID {} не найден (RequestId: {})", userId, requestId);
                    // Не выбрасываем исключение для 404, возвращаем null
                })
                .body(User.class);

            if (user != null) {
                log.info("Успешно получен пользователь: {} (ID: {}, RequestId: {})", 
                    user.getUsername(), userId, requestId);
            }
            return user;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Пользователь с ID {} не найден (RequestId: {})", userId, requestId);
                return null;
            }
            log.error("Ошибка при получении пользователя с ID {} (RequestId: {})", 
                userId, requestId, e);
            throw e;
        }
    }

    /**
     * Получает посты пользователя с использованием параметров запроса.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Использование UriBuilder для построения URL с параметрами</li>
     *     <li>Кеширование результатов по ID пользователя</li>
     *     <li>Работу с коллекцией объектов через ParameterizedTypeReference</li>
     * </ul>
     *
     * @param userId ID пользователя, чьи посты нужно получить
     * @return список постов пользователя или пустой список, если посты не найдены
     * @throws RestClientException если произошла ошибка при обращении к API
     */
    @Cacheable(value = POSTS_CACHE, key = "#userId")
    public List<Post> getUserPosts(Long userId) {
        log.info("Получение постов пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            List<Post> posts = jsonPlaceholderClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path(POSTS_PATH)
                    .queryParam("userId", userId)
                    .build())
                .header(REQUEST_ID_HEADER, requestId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Post>>() {});

            log.info("Получено {} постов для пользователя с ID {} (RequestId: {})", 
                posts != null ? posts.size() : 0, userId, requestId);
            return posts != null ? posts : Collections.emptyList();
        } catch (Exception e) {
            log.error("Ошибка при получении постов пользователя с ID {} (RequestId: {})", 
                userId, requestId, e);
            throw e;
        }
    }

    // =================================
    // POST Запросы
    // =================================

    /**
     * Создает нового пользователя.
     *
     * Демонстрирует:
     * <ul>
     *     <li>POST запрос с JSON телом</li>
     *     <li>Обработку различных HTTP статусов</li>
     *     <li>Возврат полного ResponseEntity для доступа к заголовкам</li>
     *     <li>Установку типа контента запроса</li>
     * </ul>
     *
     * @param createRequest данные для создания пользователя (валидируются на уровне контроллера)
     * @return ResponseEntity, содержащий созданного пользователя и метаданные ответа
     * @throws RestClientResponseException если произошла ошибка при создании пользователя
     */
    public ResponseEntity<User> createUser(CreateUserRequest createRequest) {
        if (createRequest == null) {
            throw new IllegalArgumentException("Данные для создания пользователя не могут быть null");
        }
        
        log.info("Создание нового пользователя: {}", createRequest.getUsername());
        String requestId = generateRequestId();

        try {
            ResponseEntity<User> response = jsonPlaceholderClient
                .post()
                .uri(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER, requestId)
                .body(createRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                    log.error("Ошибка валидации при создании пользователя: {} (RequestId: {})", 
                        responseData.getStatusCode(), requestId);
                    throw new RestClientResponseException(
                        "Ошибка валидации при создании пользователя",
                        responseData.getStatusCode(),
                        responseData.getStatusText(),
                        responseData.getHeaders(),
                        null,
                        null
                    );
                })
                .toEntity(User.class);

            User createdUser = response.getBody();
            log.info("Пользователь успешно создан: {} с ID: {} (RequestId: {})", 
                createRequest.getUsername(), 
                createdUser != null ? createdUser.getId() : "unknown", 
                requestId);
            
            return response;

        } catch (Exception e) {
            log.error("Ошибка при создании пользователя {} (RequestId: {})", 
                createRequest.getUsername(), requestId, e);
            throw e;
        }
    }

    /**
     * Отправляет данные формы на HTTPBin для демонстрации.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Отправку данных формы (application/x-www-form-urlencoded)</li>
     *     <li>Работу с Map в качестве тела запроса</li>
     *     <li>Преобразование ответа в пользовательский объект</li>
     * </ul>
     *
     * @param formData данные формы в виде пар ключ-значение
     * @return ответ от HTTPBin с информацией о запросе и отправленных данных
     * @throws RestClientException если произошла ошибка при отправке формы
     * @throws IllegalArgumentException если переданы пустые данные формы
     */
    public HttpBinResponse sendFormData(Map<String, String> formData) {
        if (formData == null || formData.isEmpty()) {
            throw new IllegalArgumentException("Данные формы не могут быть пустыми");
        }
        
        log.info("Отправка данных формы на HTTPBin: {}", formData.keySet());
        String requestId = generateRequestId();

        try {
            HttpBinResponse response = httpBinClient
                .post()
                .uri("/post")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(REQUEST_ID_HEADER, requestId)
                .body(formData)
                .retrieve()
                .body(HttpBinResponse.class);

            log.info("Форма успешно отправлена, получен ответ (RequestId: {})", requestId);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при отправке данных формы (RequestId: {})", requestId, e);
            throw e;
        }
    }

    // =================================
    // PUT и PATCH Запросы
    // =================================

    /**
     * Полностью обновляет пользователя (PUT).
     *
     * Демонстрирует:
     * <ul>
     *     <li>PUT запрос для полного обновления ресурса</li>
     *     <li>Работа с JSON в теле запроса</li>
     *     <li>Обработка ошибок при обновлении пользователя</li>
     * </ul>
     *
     * @param userId ID пользователя для обновления (должен существовать)
     * @param updateRequest данные для обновления пользователя
     * @return обновленный объект пользователя
     * @throws RestClientResponseException если произошла ошибка при обновлении пользователя
     * @throws IllegalArgumentException если данные для обновления пользователя не предоставлены
     */
    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        if (updateRequest == null) {
            throw new IllegalArgumentException("Данные для обновления пользователя не могут быть null");
        }
        
        log.info("Полное обновление пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            User updatedUser = jsonPlaceholderClient
                .put()
                .uri(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER, requestId)
                .body(updateRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                    log.error("Ошибка валидации при обновлении пользователя: {} (RequestId: {})", 
                        responseData.getStatusCode(), requestId);
                    throw new RestClientResponseException(
                        "Ошибка валидации при обновлении пользователя",
                        responseData.getStatusCode(),
                        responseData.getStatusText(),
                        responseData.getHeaders(),
                        null,
                        null
                    );
                })
                .body(User.class);

            log.info("Пользователь с ID {} успешно обновлен (RequestId: {})", userId, requestId);
            return updatedUser;

        } catch (Exception e) {
            log.error("Ошибка при полном обновлении пользователя с ID {} (RequestId: {})", 
                userId, requestId, e);
            throw e;
        }
    }

    /**
     * Частично обновляет пользователя (PATCH).
     *
     * Демонстрирует:
     * <ul>
     *     <li>PATCH запрос для частичного обновления ресурса</li>
     *     <li>Отправку Map с данными для обновления</li>
     *     <li>Обработку ошибок при частичном обновлении пользователя</li>
     * </ul>
     *
     * @param userId ID пользователя для частичного обновления
     * @param partialUpdate Map с данными для частичного обновления
     * @return обновленный объект пользователя
     * @throws RestClientResponseException если произошла ошибка при частичном обновлении пользователя
     * @throws IllegalArgumentException если данные для частичного обновления пользователя не предоставлены
     */
    public User patchUser(Long userId, Map<String, Object> partialUpdate) {
        if (partialUpdate == null || partialUpdate.isEmpty()) {
            throw new IllegalArgumentException("Данные для частичного обновления пользователя не могут быть null или пустыми");
        }
        
        log.info("Частичное обновление пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            User patchedUser = jsonPlaceholderClient
                .patch()
                .uri(USERS_PATH + "/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER, requestId)
                .body(partialUpdate)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                    log.error("Ошибка валидации при частичном обновлении пользователя: {} (RequestId: {})", 
                        responseData.getStatusCode(), requestId);
                    throw new RestClientResponseException(
                        "Ошибка валидации при частичном обновлении пользователя",
                        responseData.getStatusCode(),
                        responseData.getStatusText(),
                        responseData.getHeaders(),
                        null,
                        null
                    );
                })
                .body(User.class);

            log.info("Пользователь с ID {} успешно частично обновлен (RequestId: {})", userId, requestId);
            return patchedUser;

        } catch (Exception e) {
            log.error("Ошибка при частичном обновлении пользователя с ID {} (RequestId: {})", 
                userId, requestId, e);
            throw e;
        }
    }

    // =================================
    // DELETE Запросы
    // =================================

    /**
     * Удаляет пользователя по ID.
     *
     * Демонстрирует:
     * <ul>
     *     <li>DELETE запрос для удаления ресурса</li>
     *     <li>Обработку ситуации, когда пользователь не найден (404)</li>
     *     <li>Возврат логического значения, указывающего на успех операции</li>
     * </ul>
     *
     * @param userId ID пользователя для удаления
     * @return true, если удаление прошло успешно, false - если пользователь не найден
     * @throws RestClientResponseException если произошла ошибка при удалении пользователя (кроме 404)
     */
    public boolean deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            ResponseEntity<Void> response = jsonPlaceholderClient
                .delete()
                .uri(USERS_PATH + "/{id}", userId)
                .header(REQUEST_ID_HEADER, requestId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                    log.error("Ошибка при удалении пользователя: {} (RequestId: {})", 
                        responseData.getStatusCode(), requestId);
                    throw new RestClientResponseException(
                        "Ошибка при удалении пользователя",
                        responseData.getStatusCode(),
                        responseData.getStatusText(),
                        responseData.getHeaders(),
                        null,
                        null
                    );
                })
                .toBodilessEntity();

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Пользователь с ID {} {}удален (RequestId: {})", userId, success ? "" : "НЕ ", requestId);
            return success;

        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Пользователь с ID {} не найден для удаления (RequestId: {})", userId, requestId);
                return false;
            }
            log.error("Ошибка при удалении пользователя с ID {} (RequestId: {})", userId, requestId, e);
            throw e;
        }
    }

    // =================================
    // Асинхронные методы
    // =================================

    /**
     * Асинхронно получает пользователя по ID.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Асинхронное выполнение запроса</li>
     *     <li>Использование CompletableFuture для работы с асинхронными результатами</li>
     *     <li>Обработку ошибок при асинхронном получении пользователя</li>
     * </ul>
     *
     * @param userId ID пользователя
     * @return CompletableFuture с пользователем
     */
    @Async
    public CompletableFuture<User> getUserByIdAsync(Long userId) {
        log.info("Асинхронное получение пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            User user = getUserById(userId);
            log.info("Асинхронно получен пользователь {} с ID {} (RequestId: {})", 
                user != null ? user.getUsername() : "null", userId, requestId);
            return CompletableFuture.completedFuture(user);
        } catch (Exception e) {
            log.error("Ошибка при асинхронном получении пользователя с ID {} (RequestId: {})", 
                userId, requestId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Асинхронно получает несколько пользователей параллельно.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Параллельное выполнение нескольких асинхронных запросов</li>
     *     <li>Комбинирование результатов нескольких CompletableFuture</li>
     *     <li>Фильтрацию результатов (исключение null значений)</li>
     * </ul>
     *
     * @param userIds список ID пользователей
     * @return CompletableFuture со списком пользователей
     */
    @Async
    public CompletableFuture<List<User>> getMultipleUsersAsync(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        log.info("Асинхронное получение {} пользователей", userIds.size());
        String requestId = generateRequestId();

        List<CompletableFuture<User>> futures = userIds.stream()
            .map(this::getUserByIdAsync)
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<User> users = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(user -> user != null)
                    .toList();
                log.info("Асинхронно получено {} пользователей из {} (RequestId: {})", 
                    users.size(), userIds.size(), requestId);
                return users;
            });
    }

    // =================================
    // Демонстрация работы с заголовками и параметрами
    // =================================

    /**
     * Демонстрирует работу с заголовками запроса.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Добавление пользовательских заголовков в запрос</li>
     *     <li>Использование RequestHeadersSpec для установки заголовков</li>
     *     <li>Получение информации об отправленных заголовках через HTTPBin</li>
     * </ul>
     *
     * @param customHeaders Map с пользовательскими заголовками
     * @return HttpBinResponse с информацией о заголовках
     * @throws RestClientException если произошла ошибка при выполнении запроса
     */
    public HttpBinResponse demonstrateHeaders(Map<String, String> customHeaders) {
        log.info("Демонстрация работы с заголовками");
        String requestId = generateRequestId();

        RestClient.RequestHeadersSpec<?> request = httpBinClient
            .get()
            .uri("/headers")
            .header(REQUEST_ID_HEADER, requestId)
            .header("X-Demo-Header", "RestClient-Demo")
            .header("X-Timestamp", LocalDateTime.now().toString());

        // Добавляем пользовательские заголовки
        customHeaders.forEach(request::header);

        HttpBinResponse response = request
            .retrieve()
            .body(HttpBinResponse.class);
        
        log.info("Заголовки успешно отправлены, получен ответ (RequestId: {})", requestId);
        return response;
    }

    /**
     * Демонстрирует работу с параметрами запроса.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Добавление параметров запроса через UriBuilder</li>
     *     <li>Использование Map для передачи параметров</li>
     *     <li>Получение информации об отправленных параметрах через HTTPBin</li>
     * </ul>
     *
     * @param queryParams Map с параметрами запроса
     * @return HttpBinResponse с информацией о параметрах
     * @throws RestClientException если произошла ошибка при выполнении запроса
     */
    public HttpBinResponse demonstrateQueryParams(Map<String, String> queryParams) {
        log.info("Демонстрация работы с параметрами запроса");
        String requestId = generateRequestId();

        HttpBinResponse response = httpBinClient
            .get()
            .uri(uriBuilder -> {
                uriBuilder.path("/get");
                queryParams.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            })
            .header(REQUEST_ID_HEADER, requestId)
            .retrieve()
            .body(HttpBinResponse.class);

        log.info("Параметры успешно отправлены, получен ответ (RequestId: {})", requestId);
        return response;
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
        return REQUEST_ID_PREFIX + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Демонстрирует обработку различных типов ошибок.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Обработку исключений RestClientResponseException</li>
     *     <li>Получение информации об ошибке (статус код, текст ошибки)</li>
     *     <li>Создание объекта ошибки для возврата клиенту</li>
     * </ul>
     *
     * @param statusCode HTTP статус код для имитации
     * @return HttpBinResponse с информацией об ошибке
     */
    public HttpBinResponse demonstrateErrorHandling(int statusCode) {
        log.info("Демонстрация обработки ошибки со статусом: {}", statusCode);
        String requestId = generateRequestId();

        try {
            HttpBinResponse response = httpBinClient
                .get()
                .uri("/status/{code}", statusCode)
                .header(REQUEST_ID_HEADER, requestId)
                .retrieve()
                .body(HttpBinResponse.class);

            log.info("Запрос успешно выполнен, получен ответ (RequestId: {})", requestId);
            return response;

        } catch (RestClientResponseException e) {
            log.error("Получена ошибка HTTP {}: {} (RequestId: {})", e.getStatusCode(), e.getStatusText(), requestId);

            // Создаем объект ошибки для демонстрации
            HttpBinResponse errorResponse = HttpBinResponse.builder()
                .url("/status/" + statusCode)
                .origin("error-simulation")
                .build();

            return errorResponse;
        }
    }
}