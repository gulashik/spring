package org.gualsh.demo.gw.config.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RouteLocatorConfig {

    /**
     * Программное создание маршрутов через RouteLocator.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Базовый программный маршрут
            .route("programmatic-route", r -> r
                .path("/programmatic/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Programmatic-Route", "true")
                    .addResponseHeader("X-Custom-Header", "Programmatic-Route")
                    .addRequestParameter("source", "gateway")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с условной логикой
            .route("conditional-route", r -> r
                .path("/conditional/**")
                .and()
                .query("env", "dev|staging")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Environment", "development")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с модификацией тела запроса (ПРОГРАММНАЯ НАСТРОЙКА)
            .route("modify-request-route", r -> r
                .path("/modify-request/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .modifyRequestBody(String.class, String.class,
                        (exchange, body) -> {
                            if (body == null || body.trim().isEmpty()) {
                                body = "{}";
                            }
                            String modifiedBody = "{\n" +
                                "  \"originalRequest\": " + body + ",\n" +
                                "  \"metadata\": {\n" +
                                "    \"modifiedBy\": \"Gateway\",\n" +
                                "    \"timestamp\": \"" + java.time.LocalDateTime.now() + "\"\n" +
                                "  }\n" +
                                "}";
                            return Mono.just(modifiedBody);
                        })
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с модификацией тела ответа (ПРОГРАММНАЯ НАСТРОЙКА)
            .route("modify-response-route", r -> r
                .path("/modify-response/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .modifyResponseBody(String.class, String.class,
                        (exchange, body) -> {
                            if (body == null || body.trim().isEmpty()) {
                                body = "{}";
                            }
                            String modifiedBody = "{\n" +
                                "  \"success\": true,\n" +
                                "  \"data\": " + body + ",\n" +
                                "  \"metadata\": {\n" +
                                "    \"processedBy\": \"Gateway\",\n" +
                                "    \"timestamp\": \"" + java.time.LocalDateTime.now() + "\"\n" +
                                "  }\n" +
                                "}";
                            return Mono.just(modifiedBody);
                        })
                )
                .uri("https://httpbin.org")
            )

            .build();
    }
}
