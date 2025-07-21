package org.gualsh.demo.gw.config.gateway.route;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.gw.config.gateway.body.factory.RequestBodyModifier;
import org.gualsh.demo.gw.config.gateway.body.factory.ResponseBodyModifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Программное создание маршрутов через RouteLocator.
 */
@Slf4j
@Configuration
public class RouteLocatorConfig {

    @Bean
    public RouteLocator customRouteLocator(
        RouteLocatorBuilder builder,
        RequestBodyModifier requestBodyModifier,
        ResponseBodyModifier responseBodyModifier,
        @Qualifier("requestInfoFilter") GatewayFilter requestInfoFilter,
        @Qualifier("authenticationFilter")GatewayFilter authenticationFilter
    ) {
        // В Spring Cloud Gateway маршруты обрабатываются В ТОМ ПОРЯДКЕ, В КОТОРОМ ОНИ ОПРЕДЕЛЕНЫ.
        // ПЕРВЫЙ ПОДХОДЯЩИЙ МАРШРУТ БУДЕТ ИСПОЛЬЗОВАН, и дальнейшие маршруты проверяться не будут.
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

            // Маршрут с кастомной логикой балансировки
            .route("load-balanced-route", r -> r
                .path("/balanced/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter((exchange, chain) -> {
                        // Кастомная логика выбора backend
                        String backend = selectBackend(exchange);
                        exchange.getAttributes().put("backend", backend);
                        return chain.filter(exchange);
                    })
                    .addRequestHeader("X-Selected-Backend", "#{exchange.attributes['backend']}")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с кэшированием
            .route("cached-route", r -> r
                .path("/cached/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addResponseHeader("Cache-Control", "public, max-age=300")
                    .addResponseHeader("X-Cache-Status", "MISS")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с трансформацией данных
            .route("transform-route", r -> r
                .path("/transform/**")
                .filters(f -> f
                        .stripPrefix(1)
                        .modifyRequestBody(String.class, String.class, requestBodyModifier)
                        // или напрямую .modifyRequestBody(String.class, String.class, new RequestBodyModifier())
                        .modifyResponseBody(String.class, String.class, responseBodyModifier)
                    // или напрямую .modifyResponseBody(String.class, String.class, new ResponseBodyModifier())
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с кастомным фильтром
            .route("filter-request-info-one-route", r -> r
                .path("/request-info-filter/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(requestInfoFilter)
                )
                .uri("https://httpbin.org")
            )
            // Маршрут с блокирующим фильтром
            .route("filter-blocking-route", r -> r
                .path("/request-block-filter/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter(authenticationFilter)
                )
                .uri("https://httpbin.org")
            )

            // Маршрут если ничего не найдено. Должен быть последним!
            // ВАЖНО! Будет ПЕРЕБИВАТЬ МАРШРУТЫ из application.yml
//            .route("fallback-route", r -> r
//                .path("/**")
//                .filters(f -> f
//                    //.stripPrefix(1)
//                    .addResponseHeader("X-Gateway-Fallback", "true")
//                )
//                .uri("https://httpbin.org")
//            )

            .build();
    }

    /**
     * Кастомная логика выбора backend сервера.
     *
     * <p><strong>Этот метод демонстрирует как можно реализовать кастомную логику
     * балансировки нагрузки на основе различных факторов.</strong>
     *
     * @param exchange текущий обмен запросом
     * @return выбранный backend
     */
    private String selectBackend(ServerWebExchange exchange) {
        // Простая логика выбора backend на основе user-agent
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        if (userAgent != null && userAgent.contains("Mobile")) {
            return "mobile-backend";
        } else if (userAgent != null && userAgent.contains("Bot")) {
            return "bot-backend";
        }
        return "default-backend";
    }
}
