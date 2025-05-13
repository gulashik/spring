package org.gualsh.demo.spdrest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для документирования API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Настраивает OpenAPI документацию.
     *
     * @return настроенный объект OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components())
            .info(new Info()
                .title("Spring Data REST Demo API")
                .description("Демонстрационное API с использованием Spring Data REST")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Gualsh Demo")
                    .url("https://github.com/gualsh/spdrest-demo")
                    .email("demo@gualsh.org"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT"))
            );
    }
}