package org.gualsh.demo.gw.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Кастомный обработчик исключений для специфичных бизнес-ошибок.
 *
 * Дополнительные обработчики могут быть созданы для специфичных
 * типов исключений, требующих особой обработки.
 */
@Slf4j
@Configuration
@Order(0)
public class BusinessErrorHandler implements ErrorWebExceptionHandler {

    /**
     * Обрабатывает бизнес-специфичные исключения.
     *
     * <p><strong>Образовательный момент:</strong>
     * Этот обработчик показывает как можно создать специализированную
     * обработку для определенных типов ошибок.
     *
     * @param exchange текущий обмен
     * @param ex исключение
     * @return Mono<Void> или передача в следующий обработчик
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Обрабатываем только специфичные исключения
        if (ex instanceof BusinessException) {
            BusinessException businessEx = (BusinessException) ex;

            exchange.getResponse().setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            String errorBody = "{\n" +
                "  \"error\": true,\n" +
                "  \"business_error\": true,\n" +
                "  \"error_code\": \"" + businessEx.getErrorCode() + "\",\n" +
                "  \"message\": \"" + businessEx.getMessage() + "\",\n" +
                "  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"\n" +
                "}";

            DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(errorBody.getBytes(StandardCharsets.UTF_8));

            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        // Передаем обработку следующему обработчику
        return Mono.error(ex);
    }

    /**
     * Кастомное исключение для бизнес-ошибок.
     *
     * <p><strong>Образовательный момент:</strong>
     * Создание специализированных исключений улучшает обработку ошибок
     * и позволяет предоставлять более точную информацию клиентам.
     */
    public static class BusinessException extends RuntimeException {
        private final String errorCode;

        public BusinessException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
    }
}

