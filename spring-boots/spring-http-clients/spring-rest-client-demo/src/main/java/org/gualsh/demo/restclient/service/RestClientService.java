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
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
    @Cacheable(value = USERS_CACHE, key = ALL_USERS_CACHE_KEY) // Кеширует результат запроса
    @Retryable( // Включает механизм повторных попыток при ошибках
        retryFor = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0) // Задержка между попытками с множителем
    )
    public List<User> getAllUsers() {
        log.info("Выполнение запроса на получение всех пользователей");
        String requestId = generateRequestId();

        try {
            // Выполняем GET запрос к API
            List<User> users = jsonPlaceholderClient
                // ===Указываем конфигурацию запроса===
                .get() // Запрос будет GET
                .uri(USERS_PATH) // Путь к эндпоинту users
                .header(REQUEST_ID_HEADER, requestId) // Добавляем идентификатор запроса
                .retrieve() // Инициирует отправку HTTP-запроса, сконфигурированного предыдущими методами в цепочке

                // ===Переходим к указанию того, как обработать ответ===
                // onStatus(predicate, handler)` - настройка обработчиков для определенных HTTP-статусов
                // Обработка клиентских ошибок (4xx)
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
                // Обработка серверных ошибок (5xx)
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
                // === Получения тела ответа** различными способами ===
                //  .body(Class<T>) - преобразование тела ответа в объект указанного класса
                //  .body(ParameterizedTypeReference<T>) - преобразование тела ответа в generic-тип (например, `List<User>`)
                //  .toEntity(Class<T>) - получение полного `ResponseEntity<T>` со статусом, заголовками и телом
                // Преобразование тела ответа в List<User>
                .body(new ParameterizedTypeReference<List<User>>() {
                });

            // Логируем успешное получение пользователей
            log.info("Успешно получено {} пользователей (RequestId: {})",
                users != null ? users.size() : 0, requestId);
            return users;
    
        } catch (Exception e) {
            // Логируем ошибку и пробрасываем её дальше
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
     * Создает нового пользователя, отправляя POST запрос к внешнему API.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Выполнение POST запроса с JSON-телом</li>
     *     <li>Установку типа контента для запроса (application/json)</li>
     *     <li>Возврат полного ResponseEntity для доступа к метаданным ответа</li>
     *     <li>Валидацию входных данных</li>
     *     <li>Обработку ошибок клиентской части (4xx)</li>
     *     <li>Идентификацию запросов через уникальный request-id</li>
     * </ul>
     *
     * @param createRequest данные для создания пользователя (должны быть валидированы на уровне контроллера)
     * @return ResponseEntity с созданным пользователем и метаданными HTTP-ответа (статус, заголовки)
     * @throws IllegalArgumentException если данные для создания не предоставлены (null)
     * @throws RestClientResponseException если внешний API вернул ошибку (например, ошибку валидации)
     */
    public ResponseEntity<User> createUser(CreateUserRequest createRequest) {
        // Проверка наличия данных для создания пользователя
        if (createRequest == null) {
            throw new IllegalArgumentException("Данные для создания пользователя не могут быть null");
        }
        
        log.info("Создание нового пользователя: {}", createRequest.getUsername());
        String requestId = generateRequestId();

        try {
            // ===Выполнение POST запроса к API===
            ResponseEntity<User> response = jsonPlaceholderClient
                // Указываем, что будет POST запрос
                .post()
                // Указываем адрес (путь) для создания пользователя
                .uri(USERS_PATH)
                // Устанавливаем тип содержимого - JSON
                .contentType(MediaType.APPLICATION_JSON)
                // Добавляем идентификатор запроса для отслеживания
                .header(REQUEST_ID_HEADER, requestId)
                // Устанавливаем тело запроса - объект с данными пользователя
                .body(createRequest)
                // Инициируем отправку запроса
                .retrieve()
            
                // ===Обработка возможных ошибок===
                // Настраиваем обработку клиентских ошибок (4xx)
                .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                    // Логируем ошибку валидации
                    log.error("Ошибка валидации при создании пользователя: {} (RequestId: {})", 
                        responseData.getStatusCode(), requestId);
                    // Создаем и выбрасываем исключение с информацией об ошибке
                    throw new RestClientResponseException(
                        "Ошибка валидации при создании пользователя",
                        responseData.getStatusCode(),
                        responseData.getStatusText(),
                        responseData.getHeaders(),
                        null,
                        null
                    );
                })
            
                // ===Получение полного ответа===
                // Получаем ResponseEntity с телом ответа, преобразованным в User,
                // а также со статусом и заголовками
                .toEntity(User.class);

        // ===Обработка успешного результата===
        // Извлекаем созданного пользователя из тела ответа
        User createdUser = response.getBody();
        // Логируем успешное создание
        log.info("Пользователь успешно создан: {} с ID: {} (RequestId: {})", 
            createRequest.getUsername(), 
            createdUser != null ? createdUser.getId() : "unknown", 
            requestId);
        
        // Возвращаем полный ResponseEntity
        return response;

    } catch (Exception e) {
        // ===Обработка непредвиденных ошибок===
        // Логируем ошибку вместе с идентификатором запроса для отслеживания
        log.error("Ошибка при создании пользователя {} (RequestId: {})", 
            createRequest.getUsername(), requestId, e);
        // Пробрасываем исключение дальше для обработки на более высоком уровне
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
     * Полностью обновляет пользователя, отправляя PUT запрос к внешнему API.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Выполнение PUT запроса для полного обновления ресурса</li>
     *     <li>Использование параметра пути в URL для указания ID ресурса</li>
     *     <li>Установку типа контента для запроса (application/json)</li>
     *     <li>Валидацию входных данных</li>
     *     <li>Обработку ошибок клиентской части (4xx)</li>
     *     <li>Трассировку запросов через уникальный request-id</li>
     * </ul>
     *
     * При PUT-запросе все поля ресурса полностью заменяются данными из запроса,
     * поэтому предполагается, что updateRequest содержит все необходимые поля.
     *
     * @param userId ID пользователя для обновления (должен существовать)
     * @param updateRequest объект с данными для полного обновления пользователя
     * @return обновленный объект пользователя
     * @throws IllegalArgumentException если данные для обновления не предоставлены (null)
     * @throws RestClientResponseException если внешний API вернул ошибку (например, ресурс не найден)
     */
    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        // ===Валидация входных данных===
        if (updateRequest == null) {
            throw new IllegalArgumentException("Данные для обновления пользователя не могут быть null");
        }
        
        log.info("Полное обновление пользователя с ID: {}", userId);
        String requestId = generateRequestId();

        try {
            // ===Выполнение PUT запроса к API===
            User updatedUser = jsonPlaceholderClient
                // Указываем, что будет PUT запрос для полного обновления
                .put()
                // Указываем адрес (путь) с параметром ID пользователя
                .uri(USERS_PATH + "/{id}", userId)
                // Устанавливаем тип содержимого - JSON
                .contentType(MediaType.APPLICATION_JSON)
                // Добавляем идентификатор запроса для отслеживания
                .header(REQUEST_ID_HEADER, requestId)
                // Устанавливаем тело запроса - объект с данными пользователя
                .body(updateRequest)
                // Инициируем отправку запроса
                .retrieve()
            
            // ===Обработка возможных ошибок===
            // Настраиваем обработку клиентских ошибок (4xx)
            .onStatus(status -> status.is4xxClientError(), (request, responseData) -> {
                // Логируем ошибку валидации
                log.error("Ошибка валидации при обновлении пользователя: {} (RequestId: {})", 
                    responseData.getStatusCode(), requestId);
                // Создаем и выбрасываем исключение с информацией об ошибке
                throw new RestClientResponseException(
                    "Ошибка валидации при обновлении пользователя",
                    responseData.getStatusCode(),
                    responseData.getStatusText(),
                    responseData.getHeaders(),
                    null,
                    null
                );
            })
            
            // ===Получение тела ответа===
            // Преобразуем тело ответа в объект пользователя
            .body(User.class);

        // Логируем успешное обновление
        log.info("Пользователь с ID {} успешно обновлен (RequestId: {})", userId, requestId);
        
        // Возвращаем обновленного пользователя
        return updatedUser;

    } catch (Exception e) {
        // ===Обработка непредвиденных ошибок===
        // Логируем ошибку вместе с идентификатором запроса для отслеживания
        log.error("Ошибка при полном обновлении пользователя с ID {} (RequestId: {})", 
            userId, requestId, e);
        // Пробрасываем исключение дальше для обработки на более высоком уровне
        throw e;
    }
}

    /**
     * Частично обновляет пользователя, отправляя PATCH запрос к внешнему API.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Выполнение PATCH запроса для частичного обновления ресурса</li>
     *     <li>Использование Map для гибкой передачи только изменяемых полей</li>
     *     <li>Работу с динамическими данными в формате ключ-значение</li>
     *     <li>Валидацию входных данных</li>
     *     <li>Обработку ошибок при отсутствии ресурса (404)</li>
     *     <li>Использование уникального идентификатора для трассировки запросов</li>
     * </ul>
     *
     * В отличие от PUT-запроса, PATCH изменяет только указанные поля,
     * оставляя остальные без изменений, что делает его более эффективным
     * для частичных обновлений.
     *
     * @param userId ID пользователя для частичного обновления
     * @param partialUpdate Map с полями для обновления в формате "имя_поля": значение
     * @return обновленный объект пользователя
     * @throws IllegalArgumentException если данные для обновления не предоставлены или пусты
     * @throws RestClientResponseException если внешний API вернул ошибку
     */
    public User patchUser(Long userId, Map<String, Object> partialUpdate) {
        // ===Валидация входных данных===
        if (partialUpdate == null || partialUpdate.isEmpty()) {
            throw new IllegalArgumentException("Данные для частичного обновления не могут быть пустыми");
        }
        
        log.info("Частичное обновление пользователя с ID: {}, поля: {}", userId, partialUpdate.keySet());
        String requestId = generateRequestId();

        try {
            // ===Выполнение PATCH запроса к API===
            User updatedUser = jsonPlaceholderClient
                // Указываем, что будет PATCH запрос для частичного обновления
                .patch()
                // Указываем адрес (путь) с параметром ID пользователя
                .uri(USERS_PATH + "/{id}", userId)
                // Устанавливаем тип содержимого - JSON
                .contentType(MediaType.APPLICATION_JSON)
                // Добавляем идентификатор запроса для отслеживания
                .header(REQUEST_ID_HEADER, requestId)
                // Устанавливаем тело запроса - Map с полями для обновления
                .body(partialUpdate)
                // Инициируем отправку запроса
                .retrieve()
            
            // ===Обработка возможных ошибок===
            // Настраиваем обработку ошибки "ресурс не найден" (404)
            .onStatus(HttpStatus.NOT_FOUND::equals, (request, response) -> {
                log.error("Пользователь с ID {} не найден (RequestId: {})", userId, requestId);
                throw new RestClientResponseException(
                    "Пользователь не найден",
                    response.getStatusCode(),
                    response.getStatusText(),
                    response.getHeaders(),
                    null,
                    null
                );
            })
            
            // ===Получение тела ответа===
            // Преобразуем тело ответа в объект пользователя
            .body(User.class);

        // Логируем успешное частичное обновление
        log.info("Пользователь с ID {} частично обновлен, поля: {} (RequestId: {})", 
            userId, partialUpdate.keySet(), requestId);
        
        // Возвращаем обновленного пользователя
        return updatedUser;

    } catch (Exception e) {
        // ===Обработка непредвиденных ошибок===
        // Логируем ошибку вместе с идентификатором запроса для отслеживания
        log.error("Ошибка при частичном обновлении пользователя с ID {} (RequestId: {})", 
            userId, requestId, e);
        // Пробрасываем исключение дальше для обработки на более высоком уровне
        throw e;
    }
}

    // =================================
    // DELETE Запросы
    // =================================

    /**
     * Удаляет пользователя, отправляя DELETE запрос к внешнему API.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Выполнение DELETE запроса для удаления ресурса</li>
     *     <li>Использование статус-кода ответа для определения результата операции</li>
     *     <li>Обработку разных типов успешных ответов (204 No Content, 200 OK)</li>
     *     <li>Интерпретацию ответа без тела</li>
     *     <li>Работу с булевым результатом операции</li>
     *     <li>Отслеживание запросов через уникальный идентификатор</li>
     * </ul>
     *
     * DELETE запрос в REST API обычно возвращает статус 204 (No Content) при успехе,
     * поэтому метод интерпретирует различные успешные статус-коды и возвращает
     * булево значение, указывающее на результат операции.
     *
     * @param userId ID пользователя для удаления
     * @return true если пользователь успешно удален, false если пользователь не найден
     * @throws RestClientResponseException если произошла ошибка при удалении
     */
    public boolean deleteUser(Long userId) {
        log.info("Удаление пользователя с ID: {}", userId);
        String requestId = generateRequestId();
        
        try {
            // ===Выполнение DELETE запроса к API===
            // Некоторые API возвращают статус 204 No Content, другие 200 OK с пустым телом,
            // поэтому используем toBodilessEntity() для получения только статуса
            ResponseEntity<Void> response = jsonPlaceholderClient
                // Указываем, что будет DELETE запрос
                .delete()
                // Указываем адрес (путь) с параметром ID пользователя
                .uri(USERS_PATH + "/{id}", userId)
                // Добавляем идентификатор запроса для отслеживания
                .header(REQUEST_ID_HEADER, requestId)
                // Инициируем отправку запроса
                .retrieve()

                // ===Обработка возможных ошибок===
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

        // Проверяем статус ответа
        boolean isSuccess = response.getStatusCode().is2xxSuccessful();

        // Логируем результат операции
        if (isSuccess) {
            log.info("Пользователь с ID {} успешно удален (RequestId: {})", userId, requestId);
        } else {
            log.warn("Неожиданный статус при удалении пользователя с ID {}: {} (RequestId: {})",
                userId, response.getStatusCode(), requestId);
        }

        // Возвращаем результат операции
        return isSuccess;

    } catch (RestClientResponseException e) {
        // Если получили 404, значит пользователь не найден - возвращаем false
        if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            log.info("Пользователь с ID {} не найден при удалении (RequestId: {})", userId, requestId);
            return false;
        }

        // Для других ошибок логируем и пробрасываем исключение
        log.error("Ошибка при удалении пользователя с ID {} (RequestId: {}): {} - {}",
            userId, requestId, e.getStatusCode(), e.getMessage());
        throw e;

    } catch (Exception e) {
        // ===Обработка непредвиденных ошибок===
        log.error("Непредвиденная ошибка при удалении пользователя с ID {} (RequestId: {})",
            userId, requestId, e);
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
     *     <li>Использование Spring @Async для асинхронного выполнения в отдельном потоке</li>
     *     <li>Применение CompletableFuture для неблокирующих операций</li>
     *     <li>Делегирование запроса синхронному методу с обработкой исключений</li>
     *     <li>Трассировку запросов в асинхронном контексте</li>
     *     <li>Два способа создания CompletableFuture: успешный и с ошибкой</li>
     * </ul>
     *
     * Метод аннотирован @Async, что позволяет ему выполняться в отдельном потоке,
     * не блокируя основной поток исполнения. Это особенно полезно для длительных
     * операций или при необходимости выполнения множества запросов параллельно.
     *
     * @param userId ID пользователя для асинхронного получения
     * @return CompletableFuture, который будет содержать пользователя или ошибку
     */
    @Async
    public CompletableFuture<User> getUserByIdAsync(Long userId) {
        // Логируем начало асинхронной операции
        log.info("Асинхронное получение пользователя с ID: {}", userId);

        // Генерируем уникальный идентификатор запроса для трассировки
        String requestId = generateRequestId();

        try {
            // ===Делегируем выполнение запроса синхронному методу===
            // Это позволяет не дублировать логику получения пользователя
            User user = getUserById(userId);

            // Логируем успешное выполнение с информацией о пользователе
            log.info("Асинхронно получен пользователь {} с ID {} (RequestId: {})",
                user != null ? user.getUsername() : "null", userId, requestId);

            // ===Создаем успешно завершенный CompletableFuture===
            // Оборачиваем результат в CompletableFuture, так как метод должен
            // вернуть асинхронный результат
            return CompletableFuture.completedFuture(user);

        } catch (Exception e) {
            // ===Обработка ошибок в асинхронном контексте===
            // Логируем ошибку с идентификатором запроса для отслеживания
            log.error("Ошибка при асинхронном получении пользователя с ID {} (RequestId: {})",
                userId, requestId, e);

            // ===Создаем CompletableFuture с ошибкой===
            // Вместо возврата null или выбрасывания исключения,
            // возвращаем CompletableFuture с встроенным исключением,
            // которое будет обработано при вызове .get() или в .exceptionally()
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Асинхронно получает несколько пользователей, выполняя параллельные запросы.
     *
     * Демонстрирует:
     * <ul>
     *     <li>Композицию и оркестрацию множественных асинхронных операций</li>
     *     <li>Использование Stream API для работы с коллекциями CompletableFuture</li>
     *     <li>Применение методов allOf и join для ожидания завершения всех операций</li>
     *     <li>Фильтрацию и преобразование результатов в итоговый список</li>
     *     <li>Обработку граничных случаев (пустые входные данные)</li>
     * </ul>
     *
     * Метод позволяет существенно ускорить получение данных для множества пользователей,
     * выполняя запросы параллельно, а не последовательно. Реальный выигрыш в производительности
     * тем больше, чем больше пользователей запрашивается и чем выше задержка каждого запроса.
     *
     * @param userIds список ID пользователей для параллельного получения
     * @return CompletableFuture, который будет содержать список найденных пользователей
     */
    @Async
    public CompletableFuture<List<User>> getMultipleUsersAsync(List<Long> userIds) {
        // ===Обработка граничных случаев===
        // Проверяем наличие ID пользователей для получения
        if (userIds == null || userIds.isEmpty()) {
            // Если список пуст, возвращаем сразу завершенный CompletableFuture с пустым списком
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // Логируем начало асинхронной операции получения нескольких пользователей
        log.info("Асинхронное получение {} пользователей", userIds.size());

        // Генерируем уникальный идентификатор запроса для трассировки всей операции
        String requestId = generateRequestId();

        // ===Создание и запуск параллельных запросов===
        // Преобразуем каждый ID пользователя в асинхронный запрос
        // и собираем все эти запросы в список CompletableFuture
        List<CompletableFuture<User>> futures = userIds.stream()
            .map(this::getUserByIdAsync) // Для каждого ID создаем отдельный асинхронный запрос
            .toList();                   // Собираем все в список

        // ===Ожидание завершения всех запросов и обработка результатов===
        // CompletableFuture.allOf ожидает завершения всех переданных CompletableFuture
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            // После завершения всех запросов, преобразуем результаты
            .thenApply(v -> {
                // Извлекаем результаты из каждого CompletableFuture
                List<User> users = futures.stream()
                    .map(CompletableFuture::join)  // join получает результат каждого CompletableFuture
                    .filter(user -> user != null)   // Исключаем null-значения (не найденные пользователи)
                    .toList();                      // Собираем в итоговый список

                // Логируем результат с информацией о количестве найденных пользователей
                log.info("Асинхронно получено {} пользователей из {} (RequestId: {})",
                    users.size(), userIds.size(), requestId);

                // Возвращаем список пользователей как результат CompletableFuture
                return users;
            });
    }

    // =================================
    // Демонстрация работы с заголовками и параметрами
    // =================================

    /**
     * Демонстрирует работу с HTTP-заголовками в запросах к внешнему API.
     *
     * Метод показывает:
     * <ul>
     *     <li>Гибкое добавление статических и динамических заголовков в запрос</li>
     *     <li>Комбинирование стандартных заголовков с пользовательскими</li>
     *     <li>Использование RequestHeadersSpec для построения запроса с заголовками</li>
     *     <li>Получение информации об отправленных заголовках через HTTPBin сервис</li>
     *     <li>Присваивание метаданных запросу для трассировки и аудита</li>
     * </ul>
     *
     * HTTP-заголовки используются для передачи дополнительной информации между клиентом
     * и сервером: аутентификации, кеширования, типов контента, языковых предпочтений и т.д.
     *
     * @param customHeaders Map с пользовательскими заголовками (ключ - имя заголовка, значение - содержимое)
     * @return HttpBinResponse с информацией о полученных сервером заголовках
     * @throws RestClientException если произошла ошибка при выполнении HTTP-запроса
     */
    public HttpBinResponse demonstrateHeaders(Map<String, String> customHeaders) {
        // Логируем начало операции
        log.info("Демонстрация работы с заголовками");

        // Генерируем уникальный идентификатор запроса для трассировки
        String requestId = generateRequestId();

        // ===Построение запроса с заголовками===
        // Создаем GET запрос к эндпоинту HTTPBin, специально разработанному
        // для отражения полученных заголовков
        RestClient.RequestHeadersSpec<?> request = httpBinClient
            .get()
            .uri("/headers")

            // ===Добавление стандартных заголовков===
            // Добавляем заголовок для трассировки запроса
            .header(REQUEST_ID_HEADER, requestId)
            // Добавляем демонстрационный заголовок
            .header("X-Demo-Header", "RestClient-Demo")
            // Добавляем временную метку запроса
            .header("X-Timestamp", LocalDateTime.now().toString());

        // ===Добавление пользовательских заголовков===
        // Итерируем по Map с пользовательскими заголовками и добавляем каждый
        // в запрос, используя метод header() интерфейса RequestHeadersSpec
        customHeaders.forEach(request::header);

        // ===Выполнение запроса и обработка ответа===
        // Отправляем запрос и преобразуем JSON-ответ в объект HttpBinResponse,
        // который содержит информацию о полученных сервером заголовках
        HttpBinResponse response = request
            .retrieve()
            .body(HttpBinResponse.class);

        // Логируем успешное завершение операции с идентификатором запроса
        log.info("Заголовки успешно отправлены, получен ответ (RequestId: {})", requestId);

        // Возвращаем ответ от HTTPBin, содержащий информацию о заголовках
        return response;
    }

    /**
     * Демонстрирует работу с параметрами запроса (query parameters) в URL.
     *
     * Метод показывает:
     * <ul>
     *     <li>Построение URL с множественными параметрами запроса</li>
     *     <li>Использование UriBuilder для формирования сложных URL</li>
     *     <li>Кодирование специальных символов в параметрах запроса</li>
     *     <li>Обработку параметров с одинаковыми именами</li>
     *     <li>Получение информации о переданных параметрах через HTTPBin</li>
     * </ul>
     *
     * Параметры запроса добавляются к URL после знака вопроса (?) и разделяются
     * амперсандом (&). Они используются для фильтрации, сортировки, пагинации и
     * передачи других опциональных данных в GET-запросах.
     *
     * @param queryParams Map с параметрами запроса (ключ - имя параметра, значение - содержимое)
     * @return HttpBinResponse с информацией о полученных сервером параметрах запроса
     * @throws RestClientException если произошла ошибка при выполнении HTTP-запроса
     */
    public HttpBinResponse demonstrateQueryParams(Map<String, String> queryParams) {
        // Логируем начало операции с количеством параметров
        log.info("Демонстрация работы с параметрами запроса: {} параметров", queryParams.size());

        // Генерируем уникальный идентификатор запроса для трассировки
        String requestId = generateRequestId();

        try {
            // ===Построение URL с параметрами запроса===
            // Создаем функцию, которая будет использована для построения URL с параметрами
            Function<UriBuilder, URI> uriBuilder = builder -> {
                // Устанавливаем базовый путь
                builder.path("/get");

                // Добавляем каждый параметр из переданной Map в URL
                queryParams.forEach(builder::queryParam);

                // Добавляем служебный параметр для трассировки
                builder.queryParam("requestId", requestId);

                // Строим итоговый URI
                return builder.build();
            };

            // ===Выполнение запроса===
            // Отправляем GET запрос с построенным URL к HTTPBin
            HttpBinResponse response = httpBinClient
                .get()
                // Используем созданную функцию для построения URI
                .uri(uriBuilder)
                // Добавляем заголовок для трассировки
                .header(REQUEST_ID_HEADER, requestId)
                // Отправляем запрос
                .retrieve()
                // Преобразуем ответ в объект HttpBinResponse
                .body(HttpBinResponse.class);

            // Логируем успешное завершение операции
            log.info("Параметры запроса успешно отправлены, получен ответ (RequestId: {})", requestId);

            // Возвращаем ответ от HTTPBin
            return response;

        } catch (Exception e) {
            // Логируем ошибку при работе с параметрами запроса
            log.error("Ошибка при демонстрации параметров запроса (RequestId: {})", requestId, e);
            throw e;
        }
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