package org.gualsh.demo.gw.config.gateway.route;

import lombok.extern.slf4j.Slf4j;
import org.gualsh.demo.gw.config.gateway.body.factory.RequestBodyModifier;
import org.gualsh.demo.gw.config.gateway.body.factory.ResponseBodyModifier;
import org.gualsh.demo.gw.config.gateway.retry.RetryLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

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
        @Qualifier("authenticationFilter") GatewayFilter authenticationFilter,
        RedisRateLimiter redisRateLimiter,
        RetryLogger retryLogger
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
                        }
                    )
                    .filter((exchange, chain) -> {
                        // Кастомная логика ДО отправки запроса
                        ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-Custom-Filter", "applied")
                            .build();

                        return chain.filter(exchange.mutate().request(request).build())
                            .doFinally(signalType -> {
                                // Кастомная логика ПОСЛЕ получения ответа
                                log.info("Request completed with signal: {}", signalType);
                            });
                    })
                    //.filter(customFilterBean) // Использование готового бина

                    .addRequestHeader("X-Selected-Backend", "#{exchange.attributes['backend']}")
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

            .route("conditional-access-route", r -> r
                .path("/admins/**")
                .and()
                .header("Authorization", "Bearer .*") // проверка, есть ли такой заголовок с таким значением
                .or()
                .query("admin_token", ".*") // запрос содержит параметр admin_token с любым значением
                .or()
                .remoteAddr("192.168.1.0/24") // локальная сеть
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Admin-Access", "granted")
                )
                .uri("https://admin-api.example.com")
            )

            .route("rate-limited-service", r -> r
                .path("/rate-limited/**")
                .filters(f -> f
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter)
                        .setKeyResolver(exchange ->
                            // Можно использовать IP или другой идентификатор
                            Mono.just(exchange.getRequest()
                                .getRemoteAddress()
                                .getAddress()
                                .getHostAddress())
                        )
                    )
                    .stripPrefix(1)
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с Circuit Breaker
            // Состояния Circuit Breaker'а
            // CLOSED
            // - Все запросы проходят к backend-сервису
            // - Отслеживаются ошибки и медленные запросы
            //
            // OPEN
            // - Все запросы перенаправляются на `fallbackUri`
            // - Backend-сервис не вызывается
            //
            // HALF-OPEN
            // - Пропускается ограниченное количество тестовых запросов
            // - Если успешны → переход в CLOSED
            // - Если неуспешны → переход в OPEN
            //
            // Логика работы
            // Переход CLOSED → OPEN:
            // - ≥50% запросов завершились ошибкой ИЛИ
            // - ≥50% запросов выполнялись >2 секунд
            // - В окне минимум 5 запросов из последних 10
            //
            // Переход OPEN → HALF-OPEN:
            // - Прошло 30 секунд с момента открытия
            .route("circuit-breaker-service", r -> r
                .path("/circuit-breaker-yml/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        // Имя экземпляра Circuit Breaker'а, ссылается на конфигурацию
                        //   в разделе resilience4j.circuitbreaker.instances.demo-circuit-breaker
                        .setName("demo-circuit-breaker") // Имя экземпляра Circuit Breaker'а из application.yml
                        // Запасной маршрут при срабатывании Circuit Breaker'а
                        //   forward:/fallback - перенаправление на локальный endpoint
                        .setFallbackUri("forward:/fallback") // Запасной маршрут при срабатывании
                    )
                    .stripPrefix(1) // Удаление первого сегмента пути
                )
                .uri("https://httpbin.org")
            )

            // Основной маршрут с весовым балансировщиком
            // ~8 запросов из 10 будут направлены на httpbin.org
            .route("weighted-service", r -> r
                .path("/weighted/**")
                .and()
                .weight("group1", 8)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Weight-Group", "group1")
                )
                .uri("https://httpbin.org")
            )

            // Альтернативный маршрут для демонстрации весового балансировщика
            // ~2 запроса из 10 будут направлены на postman-echo.com
            .route("weighted-service-alt", r -> r
                .path("/weighted/**")
                .and()
                .weight("group1", 2)
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Weight-Group", "group1-alt")
                )
                .uri("https://postman-echo.com")
            )

            .route("time-based-service", r -> r
                .path("/time/**")
                .and()
                // .after(...) - маршрут активен только ПОСЛЕ указанного времени
                .after(ZonedDateTime.parse("2024-01-01T00:00:00+00:00[UTC]"))
                .and()
                // .before(...) - маршрут активен только ДО указанного времени
                .before(ZonedDateTime.parse("2024-12-31T23:59:59+00:00[UTC]"))
                .filters(f -> f
                    .stripPrefix(1)
                    .filter((exchange, chain) -> {
                        log.info("Time-based route activated at: {}", ZonedDateTime.now());
                        return chain.filter(exchange);
                    })
                    .addRequestHeader("X-Time-Based", "active")
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с Retry(механизм повторных попыток)
            .route("retry-service", r -> r.path("/retry/**")
                .filters(f -> f
                    .filter(retryLogger::logRetryAttempt) // если нужны доп. действия просто для демонстрации
                    .retry(retryConfig -> retryConfig
                        .setRetries(3) // Максимальное количество повторов
                        .setStatuses(HttpStatus.BAD_GATEWAY, HttpStatus.GATEWAY_TIMEOUT) // HTTP-статусы для повтора
                        .setMethods(HttpMethod.GET, HttpMethod.POST) // HTTP-методы, для которых разрешены повторы
                        .setBackoff(
                            Duration.ofMillis(10), // firstBackoff: первая задержка
                            Duration.ofMillis(50), // maxBackoff: максимальная задержка
                            2, // factor: коэффициент увеличения задержки
                            false // basedOnPreviousValue: расчет от базового значения
                        )
                    )
                    .stripPrefix(1)
                )
                .uri("https://httpbin.org")
            )

            // Маршрут с модификацией пути
            .route("path-rewrite-service", r -> r
                .path("/api/v1/**")
                .filters(f -> f
                    // GET /api/v1/orders/123 -> GET /orders/123 будет удален префикс /api/v1
                    .rewritePath("/api/v1/(?<path>.*)", "/${path}") // RewritePath=<регулярное_выражение>, <замена>
                    .addRequestHeader("X-Original-Path", "/api/v1") // удобно записать, что удалили
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
