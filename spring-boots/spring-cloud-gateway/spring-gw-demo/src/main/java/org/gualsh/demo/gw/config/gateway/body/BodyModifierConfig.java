package org.gualsh.demo.gw.config.gateway.body;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для модификаторов тела запросов и ответов.
 *
 * <p><strong>Образовательный момент:</strong>
 * Этот класс демонстрирует как правильно настроить Bean'ы для
 * модификаторов тела в Spring Cloud Gateway:
 * <ul>
 * <li>Правильное именование Bean'ов</li>
 * <li>Использование @Configuration для группировки</li>
 * <li>Инъекция зависимостей в фильтры</li>
 * </ul>
 *
 * <p><strong>Пример использования:</strong>
 * <pre>{@code
 * # В application.yml
 * filters:
 *   - name: ModifyRequestBody
 *     args:
 *       inClass: java.lang.String
 *       outClass: java.lang.String
 *       rewriteFunction: "#{@requestBodyModifier}"
 * }</pre>
 *
 * @author Spring Cloud Gateway Demo
 * @since 1.0.0
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