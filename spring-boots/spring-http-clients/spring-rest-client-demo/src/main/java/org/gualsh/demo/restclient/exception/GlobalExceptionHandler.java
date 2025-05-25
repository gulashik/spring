package org.gualsh.demo.restclient.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений.<p>
 *
 * Этот класс обрабатывает различные типы исключений, которые могут возникнуть
 * при работе с RestClient, и возвращает унифицированные ответы об ошибках.<p>
 *
 * Обрабатываемые типы исключений:<p>
 * - RestClientResponseException - ошибки HTTP ответов<p>
 * - RestClientException - общие ошибки RestClient<p>
 * - MethodArgumentNotValidException - ошибки валидации<p>
 * - RuntimeException - общие runtime ошибки<p>
 * - Exception - все остальные исключения<p>
 *
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения HTTP ответов от внешних сервисов.<p>
     *
     * RestClientResponseException возникает, когда внешний сервис возвращает
     * HTTP статус ошибки (4xx или 5xx).<p>
     *
     * @param ex исключение RestClientResponseException
     * @param request информация о веб-запросе
     * @return ответ с информацией об ошибке
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientResponseException(
        RestClientResponseException ex, WebRequest request) {

        log.error("RestClient HTTP ошибка: {} {}", ex.getStatusCode(), ex.getStatusText(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", ex.getStatusCode().value());
        errorResponse.put("error", getReasonPhrase(ex.getStatusCode()));
        errorResponse.put("message", "Ошибка при обращении к внешнему сервису");
        errorResponse.put("details", ex.getStatusText());
        errorResponse.put("path", request.getDescription(false));

        // Добавляем дополнительную информацию для диагностики
        if (ex.getResponseBodyAsString() != null && !ex.getResponseBodyAsString().isEmpty()) {
            errorResponse.put("externalResponse", ex.getResponseBodyAsString());
        }

        // Определяем подходящий HTTP статус для ответа
        HttpStatus responseStatus = determineResponseStatus(ex.getStatusCode());

        return ResponseEntity.status(responseStatus).body(errorResponse);
    }

    /**
     * Обрабатывает общие исключения RestClient.<p>
     *
     * RestClientException возникает при проблемах с сетью, таймаутах,
     * проблемах с сериализацией и других технических проблемах.<p>
     *
     * @param ex исключение RestClientException
     * @param request информация о веб-запросе
     * @return ответ с информацией об ошибке
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(
        RestClientException ex, WebRequest request) {

        log.error("RestClient ошибка: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("error", "Service Unavailable");
        errorResponse.put("message", "Внешний сервис временно недоступен");
        errorResponse.put("details", ex.getMessage());

        // Исправляем получение пути запроса
        String path = request.getDescription(false);
        // Убедимся, что путь не пустой
        if (path == null || path.isEmpty() || !path.startsWith("uri=")) {
            path = "uri=" + request.getContextPath();
        }
        errorResponse.put("path", path);

        errorResponse.put("retryAdvice", "Попробуйте повторить запрос позже");

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse);
    }


    /**
     * Обрабатывает ошибки валидации входных данных.<p>
     *
     * MethodArgumentNotValidException возникает когда объект, помеченный @Valid,
     * не прошел валидацию Bean Validation.<p>
     *
     * @param ex исключение валидации
     * @param request информация о веб-запросе
     * @return ответ с детальной информацией об ошибках валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
        MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Ошибка валидации входных данных: {}", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Error");
        errorResponse.put("message", "Ошибка валидации входных данных");
        errorResponse.put("path", request.getDescription(false));

        // Собираем детали ошибок валидации
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        if (!fieldErrors.isEmpty()) {
            errorResponse.put("fieldErrors", fieldErrors);
        }

        // Добавляем глобальные ошибки объекта
        if (ex.getBindingResult().hasGlobalErrors()) {
            errorResponse.put("globalErrors",
                ex.getBindingResult().getGlobalErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .toList()
            );
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Обрабатывает runtime исключения.
     *
     * @param ex runtime исключение
     * @param request информация о веб-запросе
     * @return ответ с информацией об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
        RuntimeException ex, WebRequest request) {

        log.error("Runtime ошибка: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "Внутренняя ошибка сервера");
        errorResponse.put("details", ex.getMessage());
        errorResponse.put("path", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Обрабатывает все остальные исключения.<p>
     *
     * Этот метод является fallback для всех исключений, которые не были
     * обработаны более специфичными обработчиками.<p>
     *
     * @param ex общее исключение
     * @param request информация о веб-запросе
     * @return ответ с информацией об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
        Exception ex, WebRequest request) {

        log.error("Необработанная ошибка: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "Произошла непредвиденная ошибка");
        errorResponse.put("details", "Обратитесь к администратору системы");
        errorResponse.put("path", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Обрабатывает ошибки чтения HTTP-сообщений, такие как некорректный JSON.
     *
     * @param ex исключение HttpMessageNotReadableException
     * @param request информация о веб-запросе
     * @return ответ с информацией об ошибке
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex, WebRequest request) {

        log.error("Ошибка чтения HTTP сообщения: {}", ex.getMessage());

        Map<String, Object> errorDetails = Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Некорректный формат запроса",
            "message", "Невозможно прочитать JSON запрос: возможно, некорректный формат",
            "path", request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex,
        WebRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Неподдерживаемый тип контента: " + ex.getContentType());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("details", "Поддерживаемые типы: " + ex.getSupportedMediaTypes());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Определяет подходящий HTTP статус для ответа на основе статуса внешнего сервиса.<p>
     *
     * Этот метод помогает избежать прямого проброса статусов внешних сервисов,
     * что может быть нежелательно с точки зрения безопасности и UX.<p>
     *
     * @param externalStatus HTTP статус от внешнего сервиса
     * @return подходящий HTTP статус для ответа клиенту
     */
    private HttpStatus determineResponseStatus(HttpStatusCode externalStatus) {
        return switch (externalStatus.value() / 100) {
            case 4 -> {
                // 4xx ошибки внешнего сервиса обычно указывают на проблемы с нашим запросом
                if (externalStatus.value() == 404) {
                    yield HttpStatus.NOT_FOUND;
                } else if (externalStatus.value() == 400) {
                    yield HttpStatus.BAD_REQUEST;
                } else {
                    // Для остальных 4xx ошибок возвращаем 502 (Bad Gateway)
                    yield HttpStatus.BAD_GATEWAY;
                }
            }
            case 5 -> {
                // 5xx ошибки внешнего сервиса указывают на его недоступность
                yield HttpStatus.SERVICE_UNAVAILABLE;
            }
            default -> {
                // Для всех остальных случаев
                yield HttpStatus.SERVICE_UNAVAILABLE;
            }
        };
    }

    /**
     * Получает человекочитаемое описание HTTP статуса.<p>
     *
     * @param statusCode HTTP статус код
     * @return текстовое описание статуса
     */
    private String getReasonPhrase(HttpStatusCode statusCode) {
        // Если это стандартный HttpStatus, используем его
        if (statusCode instanceof HttpStatus httpStatus) {
            return httpStatus.getReasonPhrase();
        }

        // Для нестандартных статусов создаем описание на основе кода
        return switch (statusCode.value() / 100) {
            case 1 -> "Informational";
            case 2 -> "Success";
            case 3 -> "Redirection";
            case 4 -> "Client Error";
            case 5 -> "Server Error";
            default -> "Unknown Status";
        };
    }
}