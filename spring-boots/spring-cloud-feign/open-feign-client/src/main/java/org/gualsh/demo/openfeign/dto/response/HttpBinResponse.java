package org.gualsh.demo.openfeign.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO для ответов от HttpBin API (httpbin.org).
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * HttpBin - это популярный сервис для тестирования HTTP запросов.
 * Этот DTO демонстрирует работу с динамическими структурами данных
 * и различными типами HTTP ответов.
 * </p>
 *
 * <h3>Особенности HttpBin API:</h3>
 * <ul>
 *   <li>Возвращает детальную информацию о запросе</li>
 *   <li>Эхо-сервер для тестирования различных HTTP методов</li>
 *   <li>Полезен для отладки и тестирования HTTP клиентов</li>
 *   <li>Поддерживает различные сценарии (задержки, статус коды, и т.д.)</li>
 * </ul>
 *
 * <h3>Best Practices для тестовых API:</h3>
 * <ul>
 *   <li>Используйте Map для динамических полей</li>
 *   <li>Не полагайтесь на стабильность структуры тестовых API</li>
 *   <li>Логируйте ответы для анализа</li>
 *   <li>Обрабатывайте случаи отсутствующих полей</li>
 * </ul>
 *
 * @author Generated for educational purposes
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpBinResponse {

    /**
     * URL, который был вызван.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin возвращает URL запроса, что полезно для верификации
     * того, что запрос был отправлен на правильный endpoint.
     * </p>
     */
    @JsonProperty("url")
    private String url;

    /**
     * Заголовки, которые были отправлены в запросе.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Map&lt;String, String&gt; - универсальный способ работы с HTTP заголовками.
     * Jackson автоматически десериализует JSON объект в Map.
     * </p>
     *
     * <h3>Типичные заголовки в ответе:</h3>
     * <ul>
     *   <li>User-Agent - информация о клиенте</li>
     *   <li>Accept - принимаемые типы контента</li>
     *   <li>Authorization - данные авторизации (если есть)</li>
     *   <li>Custom headers - пользовательские заголовки</li>
     * </ul>
     */
    @JsonProperty("headers")
    private Map<String, String> headers;

    /**
     * Аргументы запроса (query parameters).
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin парсит query string и возвращает параметры в виде объекта.
     * Это полезно для проверки того, что параметры были переданы корректно.
     * </p>
     */
    @JsonProperty("args")
    private Map<String, String> args;

    /**
     * Данные формы (для POST/PUT запросов).
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Для запросов с Content-Type: application/x-www-form-urlencoded
     * HttpBin возвращает данные в поле form.
     * </p>
     */
    @JsonProperty("form")
    private Map<String, String> form;

    /**
     * JSON данные запроса.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Для запросов с Content-Type: application/json HttpBin возвращает
     * parsed JSON в этом поле. Используем Object для максимальной гибкости.
     * </p>
     */
    @JsonProperty("json")
    private Object json;

    /**
     * Файлы, загруженные в запросе.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Для multipart/form-data запросов с файлами HttpBin возвращает
     * информацию о загруженных файлах.
     * </p>
     */
    @JsonProperty("files")
    private Map<String, String> files;

    /**
     * IP адрес клиента.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin возвращает IP адрес, с которого был сделан запрос.
     * Полезно для отладки проблем с proxy и load balancers.
     * </p>
     */
    @JsonProperty("origin")
    private String origin;

    /**
     * Данные о пользователе (если есть Basic Auth).
     */
    @JsonProperty("user")
    private String user;

    /**
     * Проверяет, содержит ли ответ указанный заголовок.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Utility методы для анализа ответов упрощают тестирование
     * и отладку HTTP клиентов.
     * </p>
     *
     * @param headerName имя заголовка (case-insensitive)
     * @return true, если заголовок присутствует
     */
    public boolean hasHeader(String headerName) {
        if (headers == null || headerName == null) {
            return false;
        }

        // HTTP заголовки case-insensitive, поэтому проверяем все варианты
        return headers.keySet().stream()
            .anyMatch(key -> key.equalsIgnoreCase(headerName));
    }

    /**
     * Получает значение заголовка (case-insensitive поиск).
     *
     * @param headerName имя заголовка
     * @return значение заголовка или null
     */
    public String getHeaderValue(String headerName) {
        if (headers == null || headerName == null) {
            return null;
        }

        return headers.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(headerName))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Проверяет, был ли отправлен JSON в запросе.
     *
     * @return true, если json поле не null
     */
    public boolean hasJsonData() {
        return json != null;
    }

    /**
     * Проверяет, были ли отправлены query параметры.
     *
     * @return true, если есть query параметры
     */
    public boolean hasQueryParams() {
        return args != null && !args.isEmpty();
    }

    /**
     * Проверяет, были ли отправлены form данные.
     *
     * @return true, если есть form данные
     */
    public boolean hasFormData() {
        return form != null && !form.isEmpty();
    }

    /**
     * Возвращает краткую сводку о запросе для логирования.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Методы для логирования должны быть краткими и не содержать
     * чувствительных данных (токены, пароли).
     * </p>
     *
     * @return краткое описание запроса
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("HttpBin Response: ");

        if (url != null) {
            summary.append("URL=").append(url);
        }

        if (hasQueryParams()) {
            summary.append(", QueryParams=").append(args.size());
        }

        if (hasJsonData()) {
            summary.append(", HasJSON=true");
        }

        if (hasFormData()) {
            summary.append(", FormFields=").append(form.size());
        }

        if (origin != null) {
            summary.append(", Origin=").append(origin);
        }

        return summary.toString();
    }
}
