package org.gualsh.demo.gw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Главный класс приложения Spring Cloud Gateway Demo.
 *
 * <p><strong>Образовательный момент:</strong>
 * Этот класс демонстрирует основные принципы создания Gateway приложения:
 * <ul>
 * <li>Использование @SpringBootApplication для автоконфигурации</li>
 * <li>Программное создание маршрутов через RouteLocator</li>
 * <li>Интеграция с Spring Cloud Gateway</li>
 * <li>Создание fallback endpoints</li>
 * </ul>
 */
@SpringBootApplication
//@RestController
public class GatewayDemoApplication {
    // todo Смотрим README.md
    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }

}