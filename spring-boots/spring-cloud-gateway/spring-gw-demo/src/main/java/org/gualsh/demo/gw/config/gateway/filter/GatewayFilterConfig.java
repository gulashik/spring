package org.gualsh.demo.gw.config.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class GatewayFilterConfig {

    @Bean
    public GatewayFilter requestInfoFilter () {
        return (exchange, chain) -> {
            String prefix = "X-Request-Info";

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
                .header(prefix + "-ModifedBy", "requestInfoFilter")
                .build();

            return chain.filter(exchange);
        };
    }
}
