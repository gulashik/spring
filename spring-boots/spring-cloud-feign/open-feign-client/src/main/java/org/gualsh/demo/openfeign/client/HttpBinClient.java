package org.gualsh.demo.openfeign.client;

import org.gualsh.demo.openfeign.dto.response.HttpBinResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign клиент для работы с HttpBin API.
 *
 * <h2>Образовательный момент</h2>
 * <p>
 * HttpBin (httpbin.org) - это HTTP testing service, который эхом возвращает
 * информацию о запросах. Это идеальный инструмент для изучения и тестирования
 * различных аспектов HTTP коммуникации с OpenFeign.
 * </p>
 *
 * <h3>Зачем используется HttpBin:</h3>
 * <ul>
 *   <li>Тестирование различных HTTP методов и заголовков</li>
 *   <li>Отладка проблем с сериализацией/десериализацией</li>
 *   <li>Проверка корректности передачи параметров</li>
 *   <li>Тестирование обработки ошибок и таймаутов</li>
 * </ul>
 *
 * <h3>Ключевые опции аннотации @FeignClient:</h3>
 * <ul>
 *   <li><strong>name</strong> - уникальное имя клиента в Spring контексте</li>
 *   <li><strong>url</strong> - базовый URL API (можно переопределить в конфигурации)</li>
 *   <li><strong>configuration</strong> - специфичная конфигурация для этого клиента</li>
 *   <li><strong>fallback</strong> - класс для обработки ошибок (требует Spring Cloud Circuit Breaker)</li>
 * </ul>
 *
 * <h3>Образовательная ценность:</h3>
 * <ul>
 *   <li>Демонстрация продвинутых возможностей OpenFeign</li>
 *   <li>Работа с различными Content-Type</li>
 *   <li>Передача custom заголовков</li>
 *   <li>Обработка форм и multipart данных</li>
 * </ul>
 */
@FeignClient(
    name = "httpbin",
    url = "${spring.cloud.openfeign.client.config.httpbin.url:https://httpbin.org}"
)
public interface HttpBinClient {

    /**
     * Простой GET запрос с query параметрами.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует передачу query параметров через @RequestParam.
     * HttpBin возвращает все параметры в поле "args".
     * </p>
     *
     * <h3>Особенности @RequestParam:</h3>
     * <ul>
     *   <li>Автоматическое URL encoding параметров</li>
     *   <li>Поддержка optional параметров</li>
     *   <li>Возможность передачи массивов и коллекций</li>
     * </ul>
     *
     * @param param1 первый тестовый параметр
     * @param param2 второй тестовый параметр (опциональный)
     * @return ответ HttpBin с информацией о запросе
     */
    @GetMapping("/get")
    HttpBinResponse testGet(@RequestParam("test1") String param1,
                            @RequestParam(value = "test2", required = false) String param2);

    /**
     * POST запрос с JSON данными.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает отправку произвольных JSON данных через Map.
     * HttpBin вернет отправленные данные в поле "json".
     * </p>
     *
     * <h3>Преимущества Map для тестирования:</h3>
     * <ul>
     *   <li>Гибкость в создании тестовых данных</li>
     *   <li>Не требует создания специальных DTO</li>
     *   <li>Позволяет тестировать edge cases</li>
     * </ul>
     *
     * @param data произвольные JSON данные
     * @return ответ с эхом отправленных данных
     */
    @PostMapping("/post")
    HttpBinResponse testPostJson(@RequestBody Map<String, Object> data);

    /**
     * POST запрос с form-encoded данными.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует отправку данных в формате application/x-www-form-urlencoded.
     * Важно указать правильный Content-Type в заголовке.
     * </p>
     *
     * <h3>Когда использовать form-encoded:</h3>
     * <ul>
     *   <li>Интеграция с legacy системами</li>
     *   <li>Простые формы без вложенных объектов</li>
     *   <li>Некоторые OAuth endpoints</li>
     * </ul>
     *
     * @param formData данные формы
     * @return ответ с информацией о form данных
     */
    @PostMapping(value = "/post", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    HttpBinResponse testPostForm(@RequestBody Map<String, String> formData);

    /**
     * PUT запрос с custom заголовками.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     *
     * @param apiKey    пример API ключа
     * @param requestId ID запроса для трейсинга
     * @param data      тело запроса
     * @return ответ с информацией о заголовках
     * @RequestHeader позволяет передавать пользовательские заголовки.
     * Это полезно для API ключей, трейсинга, версионирования API.
     * </p>
     *
     * <h3>Типичные use cases для custom заголовков:</h3>
     * <ul>
     *   <li>X-API-Key для авторизации</li>
     *   <li>X-Request-ID для трейсинга</li>
     *   <li>X-Client-Version для версионирования</li>
     *   <li>Accept-Language для локализации</li>
     * </ul>
     */
    @PutMapping("/put")
    HttpBinResponse testPutWithHeaders(@RequestHeader("X-API-Key") String apiKey,
                                       @RequestHeader("X-Request-ID") String requestId,
                                       @RequestBody Map<String, Object> data);

    /**
     * DELETE запрос с authorization заголовком.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Демонстрирует передачу Authorization заголовка для аутентификации.
     * HttpBin покажет, как заголовок был получен сервером.
     * </p>
     *
     * <h3>Типы Authorization:</h3>
     * <ul>
     *   <li>Bearer token - для JWT и OAuth</li>
     *   <li>Basic auth - username:password в Base64</li>
     *   <li>API Key - пользовательские схемы</li>
     * </ul>
     *
     * <h3>Security note:</h3>
     * <p>
     * В production никогда не hardcode токены в коде!
     * Используйте конфигурацию или RequestInterceptor.
     * </p>
     *
     * @param authorization заголовок авторизации (например, "Bearer token123")
     * @return ответ с информацией об авторизации
     */
    @DeleteMapping("/delete")
    HttpBinResponse testDeleteWithAuth(@RequestHeader("Authorization") String authorization);

    /**
     * PATCH запрос для частичного обновления.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * PATCH метод используется для частичного обновления ресурсов.
     * В отличие от PUT, который заменяет весь ресурс.
     * </p>
     *
     * @param data частичные данные для обновления
     * @return ответ с информацией о PATCH запросе
     */
    @PatchMapping("/patch")
    HttpBinResponse testPatch(@RequestBody Map<String, Object> data);

    /**
     * Тестирование обработки различных HTTP статус кодов.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin позволяет принудительно возвращать различные статус коды.
     * Это полезно для тестирования обработки ошибок в вашем ErrorDecoder.
     * </p>
     *
     * <h3>Полезные статус коды для тестирования:</h3>
     * <ul>
     *   <li>200 - успешный запрос</li>
     *   <li>400 - неверный запрос</li>
     *   <li>401 - требуется авторизация</li>
     *   <li>404 - ресурс не найден</li>
     *   <li>500 - внутренняя ошибка сервера</li>
     *   <li>503 - сервис недоступен</li>
     * </ul>
     *
     * @param statusCode желаемый HTTP статус код
     * @return ответ с указанным статус кодом
     */
    @GetMapping("/status/{code}")
    HttpBinResponse testStatusCode(@PathVariable("code") int statusCode);

    /**
     * Тестирование задержки ответа.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin может искусственно задерживать ответ на указанное количество секунд.
     * Это полезно для тестирования таймаутов и retry логики.
     * </p>
     *
     * <h3>Тестирование сценариев:</h3>
     * <ul>
     *   <li>Проверка connect timeout (короткая задержка)</li>
     *   <li>Проверка read timeout (длинная задержка)</li>
     *   <li>Тестирование retry механизмов</li>
     *   <li>Проверка user experience при медленных ответах</li>
     * </ul>
     *
     * <h3>Безопасность:</h3>
     * <p>
     * Не используйте большие значения delay в production тестах!
     * Максимум 2-3 секунды для автотестов.
     * </p>
     *
     * @param seconds количество секунд задержки (максимум 10)
     * @return ответ после указанной задержки
     */
    @GetMapping("/delay/{seconds}")
    HttpBinResponse testDelay(@PathVariable("seconds") int seconds);

    /**
     * Получение информации об IP адресе клиента.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Простой endpoint для получения внешнего IP адреса.
     * Полезен для отладки проблем с proxy и NAT.
     * </p>
     *
     * @return ответ с IP адресом клиента
     */
    @GetMapping("/ip")
    HttpBinResponse getClientIP();

    /**
     * Получение User-Agent клиента.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * Показывает, какой User-Agent отправляет ваш Feign клиент.
     * Полезно для проверки работы RequestInterceptor.
     * </p>
     *
     * @return ответ с информацией о User-Agent
     */
    @GetMapping("/user-agent")
    HttpBinResponse getUserAgent();

    /**
     * Тестирование Basic Authentication.
     *
     * <h2>Образовательный момент</h2>
     * <p>
     * HttpBin проверяет Basic Auth credentials и возвращает успех/ошибку.
     * Демонстрирует работу с аутентификацией через заголовки.
     * </p>
     *
     * <h3>Basic Auth формат:</h3>
     * <p>
     * Authorization: Basic base64(username:password)
     * </p>
     *
     * @param credentials Base64 encoded "username:password"
     * @return ответ с результатом аутентификации
     */
    @GetMapping("/basic-auth/{user}/{passwd}")
    HttpBinResponse testBasicAuth(@RequestHeader("Authorization") String credentials,
                                  @PathVariable("user") String user,
                                  @PathVariable("passwd") String passwd);
}