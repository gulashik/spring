package org.gualsh.demo.gw.config.gateway.body.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модификатор тела ответа для демонстрации.
 * <p>
 * <ul>
 * <li>Добавления метаданных</li>
 * <li>Унификации формата ответов</li>
 * <li>Фильтрации чувствительных данных</li>
 * <li>Преобразования форматов</li>
 * </ul>
 */
@Slf4j
public class ResponseBodyModifier implements RewriteFunction<String, String> {

    /**
     * Модифицирует тело ответа, добавляя обертку с метаданными.
     *
     * @param exchange текущий обмен
     * @param body оригинальное тело ответа
     * @return Publisher с модифицированным телом ответа
     */
    @Override
    public Publisher<String> apply(ServerWebExchange exchange, String body) {
        if (body == null || body.trim().isEmpty()) {
            body = "{}";
        }
        log.info("Body before: {}", body);
        try {
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                exchange.getResponse().getStatusCode().value() : 200;

            String modifiedBody = "{\n" +
                "  \"success\": " + (statusCode >= 200 && statusCode < 300) + ",\n" +
                "  \"data\": " + body + ",\n" +
                "  \"metadata\": {\n" +
                "    \"processedBy\": \"------>ResponseBodyModifier<-----\",\n" +
                "    \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",\n" +
                "    \"requestId\": \"" + exchange.getRequest().getHeaders().getFirst("X-Request-ID") + "\",\n" +
                "    \"statusCode\": " + statusCode + "\n" +
                "  }\n" +
                "}";

            return Mono.just(modifiedBody);
        } catch (Exception e) {
            // В случае ошибки возвращаем оригинальное тело
            return Mono.just(body != null ? body : "{}");
        }
    }
}