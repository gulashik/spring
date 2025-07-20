package org.gualsh.demo.gw.config.gateway.body;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для модификаторов тела запросов и ответов.
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * // Маршрут с трансформацией данных
 * .route("transform-route", r -> r
 *     .path("/transform/**")
 *     .filters(f -> f
 *         .stripPrefix(1)
 *         .modifyRequestBody(String.class, String.class, requestBodyModifier)
 *         // или напрямую .modifyRequestBody(String.class, String.class, new RequestBodyModifier())
 *         .modifyResponseBody(String.class, String.class, responseBodyModifier)
 *         // или напрямую .modifyResponseBody(String.class, String.class, new ResponseBodyModifier())
 *     )
 *     .uri("https://httpbin.org")
 * )
 * }</pre>
 */
@Configuration
public class BodyModifierConfig {

    /**
     * Bean для модификации тела запроса.
     *
     * <p><strong>Образовательный момент:</strong>
     * Именование Bean'а важно для правильной работы SpEL выражений
     * в конфигурации Gateway.
     *
     * @return экземпляр RequestBodyModifier
     */
    @Bean
    public RequestBodyModifier requestBodyModifier() {
        return new RequestBodyModifier();
    }

    /**
     * Bean для модификации тела ответа.
     *
     * <p><strong>Образовательный момент:</strong>
     * ResponseBodyModifier должен быть thread-safe, так как
     * может использоваться одновременно несколькими потоками.
     *
     * @return экземпляр ResponseBodyModifier
     */
    @Bean
    public ResponseBodyModifier responseBodyModifier() {
        return new ResponseBodyModifier();
    }
}
