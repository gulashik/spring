package org.gualsh.demo.openfeign.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Кастомный декодер ошибок для обработки HTTP ошибок от внешних API.
 *
 * <h5>ErrorDecoder</h5>
 * <p>
 * ErrorDecoder - это ключевой компонент для обработки ошибок в OpenFeign.
 * Он позволяет преобразовывать HTTP ошибки в типизированные Java исключения,
 * что улучшает обработку ошибок в приложении.
 * </p>
 *
 * <h5>Зачем нужен кастомный ErrorDecoder:</h5>
 * <ul>
 *   <li>Преобразование HTTP кодов в специфичные исключения</li>
 *   <li>Извлечение дополнительной информации из тела ответа</li>
 *   <li>Логирование ошибок для мониторинга</li>
 *   <li>Интеграция с глобальной системой обработки ошибок</li>
 * </ul>
 *
 * <h5>Best Practices:</h5>
 * <ul>
 *   <li>Всегда логируйте детали ошибки для отладки</li>
 *   <li>Не выбрасывайте чувствительные данные в исключениях</li>
 *   <li>Используйте стандартные Spring исключения для консистентности</li>
 *   <li>Обрабатывайте случаи, когда тело ответа пустое или некорректное</li>
 * </ul>
 *
 * <h5>Подводные камни:</h5>
 * <ul>
 *   <li>Response.Body можно прочитать только один раз</li>
 *   <li>Необходимо корректно закрывать InputStream</li>
 *   <li>Обрабатывать случаи IOException при чтении тела ответа</li>
 * </ul>
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    /**
     * {@link feign.codec.ErrorDecoder.Default#Default()} предоставляет стандартную логику преобразования HTTP ошибок
     *      в соответствующие Java исключения согласно статус-кодам ответа.
     *  <p>
     *  ErrorDecoder.Default создает FeignException(наследник RuntimeException) для всех не-2xx ответов.
     *  <p>
     *  Стандартный декодер для случаев, когда требуется базовое поведение
     * */
    private final ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();

    /**
     * @param objectMapper Jackson ObjectMapper для десериализации JSON
     */
    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Декодирует HTTP ошибку в Java исключение.
     *
     * <p>
     * Этот метод вызывается для каждого HTTP ответа с кодом не 2xx.
     * Здесь можно:
     * </p>
     * <ul>
     *   <li>Анализировать код ответа и заголовки</li>
     *   <li>Читать тело ответа для получения деталей ошибки</li>
     *   <li>Создавать специфичные исключения</li>
     *   <li>Логировать ошибки для мониторинга</li>
     * </ul>
     *
     * @param methodKey ключ метода (для идентификации источника ошибки)
     * @param response HTTP ответ с ошибкой
     * @return исключение, которое будет выброшено
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        String requestUrl = response.request().url();

        // Читаем тело ответа для получения деталей ошибки
        String responseBody = getResponseBody(response);

        // Логируем информацию об ошибке
        log.error("Feign client error - Method: {}, Status: {}, URL: {}, Body: {}",
            methodKey, response.status(), requestUrl, responseBody
        );

        // Обрабатываем различные типы HTTP ошибок
        return switch (httpStatus.series()) {
            case CLIENT_ERROR -> handleClientError(httpStatus, methodKey, requestUrl, responseBody);
            case SERVER_ERROR -> handleServerError(httpStatus, methodKey, requestUrl, responseBody);
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }

    /**
     * Обрабатывает <b style="color: red";>клиентские ошибки (4xx)</b>.
     *
     * <p>
     * Клиентские ошибки обычно указывают на проблемы с запросом:
     * неправильные параметры, отсутствие авторизации, несуществующий ресурс.
     * </p>
     *
     * @param status HTTP статус
     * @param methodKey ключ метода
     * @param url URL запроса
     * @param responseBody тело ответа
     * @return соответствующее исключение
     */
    private Exception handleClientError(HttpStatus status, String methodKey, String url, String responseBody) {
        String message = String.format("Client error in %s: %s", methodKey, status.getReasonPhrase());

        return switch (status) {
            case BAD_REQUEST -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message + " - Invalid request parameters. Response: " + responseBody
            );
            case UNAUTHORIZED -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                message + " - Authentication required or invalid credentials"
            );
            case FORBIDDEN -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                message + " - Access denied to resource"
            );
            case NOT_FOUND -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                message + " - Resource not found: " + url
            );
            case TOO_MANY_REQUESTS -> new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                message + " - Rate limit exceeded. Please retry later"
            );
            default -> new ResponseStatusException(
                status,
                message + " - " + responseBody
            );
        };
    }

    /**
     * Обрабатывает <b style="color: red";>серверные ошибки (5xx)</b>.
     *
     * <p>
     * Серверные ошибки указывают на проблемы на стороне внешнего сервиса.
     * Для таких ошибок часто имеет смысл использовать retry механизм.
     * </p>
     *
     * @param status HTTP статус
     * @param methodKey ключ метода
     * @param url URL запроса
     * @param responseBody тело ответа
     * @return соответствующее исключение
     */
    private Exception handleServerError(HttpStatus status, String methodKey, String url, String responseBody) {
        String message = String.format("Server error in %s: %s", methodKey, status.getReasonPhrase());

        return switch (status) {
            case INTERNAL_SERVER_ERROR -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message + " - Internal server error in external service"
            );
            case BAD_GATEWAY -> new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                message + " - External service is unavailable"
            );
            case SERVICE_UNAVAILABLE -> new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                message + " - External service is temporarily unavailable"
            );
            case GATEWAY_TIMEOUT -> new ResponseStatusException(
                HttpStatus.GATEWAY_TIMEOUT,
                message + " - Timeout when calling external service"
            );
            default -> new ResponseStatusException(
                status,
                message + " - " + responseBody
            );
        };
    }

    /**
     * Безопасно читает тело HTTP ответа и пытается извлечь структурированную информацию об ошибке.
     *
     * <h2>Важный момент</h2>
     * <p>
     * Чтение тела ответа в ErrorDecoder требует осторожности:
     * </p>
     * <ul>
     *   <li>Response.Body может быть null</li>
     *   <li>InputStream можно прочитать только один раз</li>
     *   <li>Необходимо корректно обрабатывать IOException</li>
     *   <li>Ресурсы должны быть закрыты</li>
     *   <li>JSON может быть некорректным или отсутствовать</li>
     * </ul>
     *
     * @param response HTTP ответ
     * @return строковое представление тела ответа
     */
    private String getResponseBody(Response response) {
        try {
            if (response.body() == null) {
                return "Empty response body";
            }

            try (InputStream inputStream = response.body().asInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                String responseBody = new String(bytes, StandardCharsets.UTF_8);

                // Пытаемся извлечь структурированную информацию об ошибке из JSON
                try {
                    // Попытка парсинга как JSON объект для извлечения деталей ошибки
                    var jsonNode = objectMapper.readTree(responseBody);

                    // Ищем стандартные поля ошибок в JSON ответе
                    String errorMessage = extractErrorMessage(jsonNode);
                    if (errorMessage != null) {
                        return errorMessage;
                    }

                    // Если специфическое сообщение не найдено, возвращаем весь JSON
                    return responseBody;

                } catch (Exception jsonParseException) {
                    // Если JSON парсинг не удался, возвращаем сырой текст
                    log.debug("Failed to parse error response as JSON: {}", jsonParseException.getMessage());
                    return responseBody;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read response body", e);
            return "Failed to read response body: " + e.getMessage();
        }
    }

    /**
     * Извлекает сообщение об ошибке из JSON структуры.
     *
     * <p>
     * Различные API используют разные форматы для ошибок:
     * </p>
     * <ul>
     *   <li>Spring Boot: {"error": "Not Found", "message": "Details"}</li>
     *   <li>RFC 7807: {"type": "...", "title": "...", "detail": "..."}</li>
     *   <li>Custom API: {"errorMessage": "...", "code": "..."}</li>
     * </ul>
     *
     * @param jsonNode корневой узел JSON ответа
     * @return извлеченное сообщение об ошибке или null
     */
    private String extractErrorMessage(com.fasterxml.jackson.databind.JsonNode jsonNode) {
        // Стандартные поля ошибок в порядке приоритета
        String[] errorFields = {
            "message",      // Spring Boot standard
            "detail",       // RFC 7807 Problem Details
            "error",        // Generic error field
            "errorMessage", // Custom API format
            "description",  // Alternative description field
            "title"         // RFC 7807 title
        };

        for (String field : errorFields) {
            var fieldNode = jsonNode.get(field);
            if (fieldNode != null && !fieldNode.isNull() && fieldNode.isTextual()) {
                String errorText = fieldNode.asText();
                if (!errorText.trim().isEmpty()) {
                    log.debug("Extracted error message from field '{}': {}", field, errorText);
                    return errorText;
                }
            }
        }

        return null;
    }
}