package org.gualsh.demo.gw.config.gateway.body.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модификатор тела запроса для демонстрации.
 * <strong style="color:red">Класс должен быть потокобезопасным, так как один экземпляр может обрабатывать
 * множественные запросы одновременно</strong>
 * <p>
 * <ul>
 * <li>Добавления метаданных</li>
 * <li>Унификации формата ответов</li>
 * <li>Фильтрации чувствительных данных</li>
 * <li>Преобразования форматов</li>
 * </ul>
 */
@Slf4j
public class RequestBodyModifier implements RewriteFunction<String, String> {

    /**
     * Модифицирует тело запроса, добавляя метаданные.
     *
     * @param exchange текущий обмен
     * @param body оригинальное тело запроса
     * @return Publisher с модифицированным телом запроса
     */
    @Override
    public Publisher<String> apply(ServerWebExchange exchange, String body) {
        if (body == null || body.trim().isEmpty()) {
            body = "{}";
        }
        log.info("Body before: {}", body);
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


