package org.gualsh.demo.webclient.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Утилитные методы для работы с HTTP статусами в разных версиях Spring.
 *
 * <p>Обеспечивает совместимость между Spring Boot 2.x и 3.x, где
 * HttpStatusCode и HttpStatus имеют разные API.</p>
 */
@Slf4j
public final class HttpStatusUtils {

    private HttpStatusUtils() {
        // Утилитный класс - не должен создаваться
    }

    /**
     * Безопасно преобразует HttpStatusCode в HttpStatus.
     *
     * <p>В Spring 6.x HttpStatusCode - это интерфейс, а HttpStatus - enum.
     * Этот метод безопасно выполняет преобразование.</p>
     *
     * @param statusCode HttpStatusCode для преобразования
     * @return HttpStatus или INTERNAL_SERVER_ERROR если преобразование невозможно
     */
    public static HttpStatus toHttpStatus(HttpStatusCode statusCode) {
        if (statusCode == null) {
            log.warn("Null status code provided, using INTERNAL_SERVER_ERROR");
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        try {
            // Если это уже HttpStatus, возвращаем как есть
            if (statusCode instanceof HttpStatus) {
                return (HttpStatus) statusCode;
            }

            // Пытаемся найти соответствующий HttpStatus по коду
            return HttpStatus.valueOf(statusCode.value());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown HTTP status code: {}, using INTERNAL_SERVER_ERROR", statusCode.value());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Безопасно получает reason phrase для HTTP статуса.
     *
     * @param statusCode HTTP статус код
     * @return reason phrase или пустую строку
     */
    public static String getReasonPhrase(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return "";
        }

        try {
            if (statusCode instanceof HttpStatus) {
                return ((HttpStatus) statusCode).getReasonPhrase();
            }

            // Пытаемся преобразовать в HttpStatus
            HttpStatus httpStatus = HttpStatus.valueOf(statusCode.value());
            return httpStatus.getReasonPhrase();
        } catch (Exception e) {
            log.debug("Could not get reason phrase for status {}: {}", statusCode, e.getMessage());
            return "";
        }
    }

    /**
     * Проверяет, является ли статус клиентской ошибкой (4xx).
     *
     * @param statusCode HTTP статус код
     * @return true если это 4xx ошибка
     */
    public static boolean is4xxClientError(HttpStatusCode statusCode) {
        return statusCode != null && statusCode.is4xxClientError();
    }

    /**
     * Проверяет, является ли статус серверной ошибкой (5xx).
     *
     * @param statusCode HTTP статус код
     * @return true если это 5xx ошибка
     */
    public static boolean is5xxServerError(HttpStatusCode statusCode) {
        return statusCode != null && statusCode.is5xxServerError();
    }

    /**
     * Проверяет, является ли статус успешным (2xx).
     *
     * @param statusCode HTTP статус код
     * @return true если это 2xx статус
     */
    public static boolean is2xxSuccessful(HttpStatusCode statusCode) {
        return statusCode != null && statusCode.is2xxSuccessful();
    }

    /**
     * Получает числовое значение статуса безопасно.
     *
     * @param statusCode HTTP статус код
     * @return числовое значение или 500 если null
     */
    public static int getValue(HttpStatusCode statusCode) {
        return statusCode != null ? statusCode.value() : 500;
    }

    /**
     * Создает user-friendly сообщение для статуса.
     *
     * @param statusCode HTTP статус код
     * @return понятное пользователю сообщение
     */
    public static String getDisplayMessage(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return "Unknown error occurred";
        }

        int code = statusCode.value();
        return switch (code) {
            case 400 -> "Invalid request data";
            case 401 -> "Authentication required";
            case 403 -> "Access forbidden";
            case 404 -> "Resource not found";
            case 429 -> "Too many requests - please slow down";
            case 500 -> "Internal server error";
            case 502 -> "Bad gateway";
            case 503 -> "Service temporarily unavailable";
            case 504 -> "Gateway timeout";
            default -> {
                if (is4xxClientError(statusCode)) {
                    yield "Client error (" + code + ")";
                } else if (is5xxServerError(statusCode)) {
                    yield "Server error (" + code + ")";
                } else {
                    yield "HTTP " + code;
                }
            }
        };
    }

    /**
     * Определяет, должен ли быть выполнен retry для данного статуса.
     *
     * @param statusCode HTTP статус код
     * @return true если имеет смысл повторить запрос
     */
    public static boolean shouldRetry(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return true; // Retry для неизвестных ошибок
        }

        int code = statusCode.value();
        return switch (code) {
            // Retry для серверных ошибок
            case 500, 502, 503, 504 -> true;
            // Retry для rate limiting (с задержкой)
            case 429 -> true;
            // НЕ retry для клиентских ошибок
            case 400, 401, 403, 404 -> false;
            // По умолчанию retry только для 5xx
            default -> is5xxServerError(statusCode);
        };
    }

    /**
     * Получает рекомендуемую задержку для retry (в секундах).
     *
     * @param statusCode HTTP статус код
     * @param attempt номер попытки (начиная с 1)
     * @return задержка в секундах
     */
    public static long getRetryDelay(HttpStatusCode statusCode, int attempt) {
        if (statusCode == null) {
            return Math.min(1L << attempt, 60); // Exponential backoff до 60 сек
        }

        int code = statusCode.value();
        return switch (code) {
            case 429 -> 60; // Rate limiting - дольше ждем
            case 503 -> Math.min(5L << attempt, 120); // Service unavailable
            case 504 -> Math.min(2L << attempt, 30); // Gateway timeout
            default -> Math.min(1L << attempt, 60); // Стандартный backoff
        };
    }

    /**
     * Проверяет, критична ли ошибка (требует немедленного внимания).
     *
     * @param statusCode HTTP статус код
     * @return true если ошибка критична
     */
    public static boolean isCriticalError(HttpStatusCode statusCode) {
        if (statusCode == null) {
            return true;
        }

        int code = statusCode.value();
        return switch (code) {
            // Критичные серверные ошибки
            case 500, 502, 504 -> true;
            // Проблемы с аутентификацией
            case 401, 403 -> true;
            // Некритичные ошибки
            case 400, 404, 429, 503 -> false;
            // По умолчанию серверные ошибки критичны
            default -> is5xxServerError(statusCode);
        };
    }
}