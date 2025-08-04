package org.gualsh.demo.curbreaker.exception;

/**
 * Исключение для ошибок внешних сервисов.
 *
 * <h3>Образовательный момент:</h3>
 * <p>
 * Создание специфичных исключений для разных типов ошибок позволяет более точно
 * настраивать Circuit Breaker. Можно указывать, какие исключения должны учитываться
 * при расчете failure rate, а какие - игнорироваться.
 * </p>
 *
 * <p><strong>Типы исключений в контексте Circuit Breaker:</strong></p>
 * <ul>
 *   <li>Technical exceptions - network errors, timeouts (учитываются)</li>
 *   <li>Business exceptions - validation, business logic (игнорируются)</li>
 *   <li>Security exceptions - authentication, authorization (могут игнорироваться)</li>
 * </ul>
 *
 * <p><strong>Конфигурация в application.yml:</strong></p>
 * <pre>{@code
 * resilience4j:
 *   circuitbreaker:
 *     instances:
 *       externalApi:
 *         record-exceptions:
 *           - org.gualsh.demo.curbreaker.exception.ExternalServiceException
 *         ignore-exceptions:
 *           - java.lang.IllegalArgumentException
 * }</pre>
 *
 * <p><strong>Пример использования:</strong></p>
 * <pre>{@code
 * if (response.getStatusCode().isError()) {
 *     throw new ExternalServiceException(
 *         "External API returned error: " + response.getStatusCode(),
 *         response.getBody()
 *     );
 * }
 * }</pre>
 *
 * @author Educational Demo
 */
public class ExternalServiceException extends RuntimeException {

    /**
     * Дополнительная информация об ошибке (например, response body).
     */
    private final String additionalInfo;

    /**
     * Создание исключения с сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ExternalServiceException(String message) {
        super(message);
        this.additionalInfo = null;
    }

    /**
     * Создание исключения с сообщением и причиной.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
        this.additionalInfo = null;
    }

    /**
     * Создание исключения с сообщением и дополнительной информацией.
     *
     * <p><strong>Образовательный момент:</strong></p>
     * <p>
     * Дополнительная информация может содержать response body от внешнего сервиса,
     * что помогает в диагностике проблем. Важно не логировать sensitive данные.
     * </p>
     *
     * @param message сообщение об ошибке
     * @param additionalInfo дополнительная информация
     */
    public ExternalServiceException(String message, String additionalInfo) {
        super(message);
        this.additionalInfo = additionalInfo;
    }

    /**
     * Создание исключения со всеми параметрами.
     *
     * @param message сообщение об ошибке
     * @param cause причина ошибки
     * @param additionalInfo дополнительная информация
     */
    public ExternalServiceException(String message, Throwable cause, String additionalInfo) {
        super(message, cause);
        this.additionalInfo = additionalInfo;
    }

    /**
     * Получение дополнительной информации об ошибке.
     *
     * @return дополнительная информация или null
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
