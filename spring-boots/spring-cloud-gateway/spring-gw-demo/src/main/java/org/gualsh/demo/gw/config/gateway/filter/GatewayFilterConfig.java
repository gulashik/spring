package org.gualsh.demo.gw.config.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конфигурация Gateway фильтров(уровень маршрута).
 * <p>
 * <b style="color:red">Gateway фильтры (в отличие от Global фильтров) применяются селективно
 * к конкретным маршрутам</b>, что обеспечивает более гибкую настройку обработки запросов.
 *
 * @see org.springframework.cloud.gateway.filter.GatewayFilter
 * @see GlobalFilterConfig
 */
@Configuration
public class GatewayFilterConfig {

    /**
     * Gateway фильтр для добавления информации о запросе в заголовки.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот фильтр демонстрирует добавление метаинформации о запросе
     * в заголовки HTTP для целей отладки и мониторинга. Фильтр добавляет:
     * <ul>
     * <li>HTTP метод запроса</li>
     * <li>Путь запроса</li>
     * <li>Query параметры</li>
     * <li>Адрес клиента</li>
     * <li>Временную метку обработки</li>
     * <li>Идентификатор фильтра</li>
     * </ul>
     *
     * @return настроенный Gateway фильтр для добавления информации о запросе
     */
    @Bean
    public GatewayFilter requestInfoFilter() {
        return (exchange, chain) -> {
            String prefix = "X-Gateway-Request-Info";

            // Добавляем информацию о запросе в заголовки
            exchange.getRequest().mutate()
                .header(prefix + "-Method", exchange.getRequest().getMethod().name())
                .header(prefix + "-Path", exchange.getRequest().getPath().value())
                .header(prefix + "-Query", exchange.getRequest().getQueryParams().toString())
                .header(prefix + "-Remote-Address",
                    exchange.getRequest().getRemoteAddress() != null ?
                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                        "unknown")
                .header(prefix + "-Timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .header(prefix + "-ModifedBy", "------>requestInfoFilter<------")
                .build();

            return chain.filter(exchange);
        };
    }

    @Bean
    public GatewayFilter authenticationFilter() {
        return (exchange, chain) -> {
            // Проверяем наличие токена авторизации
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // Отклоняем запрос с кодом 401
                ServerHttpResponse response = exchange.getResponse();
                // Установите соответствующий HTTP статус через `response.setStatusCode()`
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");

                // Опционально добавьте тело ответа с описанием ошибки
                String body = "{\"error\": \"Authentication required\"}";
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

                // Не вызывайте chain.filter(exchange) если хотите отклонить запрос
                return response.writeWith(Mono.just(buffer));
            }

            // Если авторизация прошла, продолжаем цепочку
            return chain.filter(exchange);
        };
    }
}