package org.gualsh.demo.gw.filter;

import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модификатор тела запроса для демонстрации.
 *
 * <p><strong>Образовательный момент:</strong>
 * RewriteFunction используется для модификации тела запроса/ответа.
 * Этот класс должен быть thread-safe и корректно реализовывать интерфейс.
 */
public class RequestBodyModifier implements RewriteFunction<String, String> {

    /**
     * Модифицирует тело запроса, добавляя метаданные.
     *
     * <p><strong>Образовательный момент:</strong>
     * При модификации тела запроса важно:
     * <ul>
     * <li>Обрабатывать null и пустые значения</li>
     * <li>Сохранять валидность JSON/XML</li>
     * <li>Учитывать размер данных</li>
     * <li>Обрабатывать ошибки парсинга</li>
     * </ul>
     *
     * @param exchange текущий обмен
     * @param body оригинальное тело запроса
     * @return Publisher с модифицированным телом запроса
     */
    @Override
    public org.reactivestreams.Publisher<String> apply(ServerWebExchange exchange, String body) {
        if (body == null || body.trim().isEmpty()) {
            body = "{}";
        }

        try {
            // Простая обертка JSON
            String modifiedBody = "{\n" +
                "  \"originalRequest\": " + body + ",\n" +
                "  \"metadata\": {\n" +
                "    \"modifiedBy\": \"RequestBodyModifier\",\n" +
                "    \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                "    \"requestId\": \"" + exchange.getRequest().getHeaders().getFirst("X-Request-ID") + "\"\n" +
                "  }\n" +
                "}";

            return Mono.just(modifiedBody);
        } catch (Exception e) {
            // В случае ошибки возвращаем оригинальное тело
            return Mono.just(body != null ? body : "{}");
        }
    }
}


