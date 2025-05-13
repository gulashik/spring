package org.gualsh.demo.spdrest.config;

import org.gualsh.demo.spdrest.model.Author;
import org.gualsh.demo.spdrest.model.Book;
import org.gualsh.demo.spdrest.model.Category;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ExposureConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Конфигурация Spring Data REST.
 *
 * Настраивает поведение REST API, включая:
 * - экспозицию ID в JSON-ответах
 * - базовый путь API
 * - разрешенные операции для сущностей
 * - CORS-настройки
 */
@Configuration
public class SpringDataRestConfig {

    /**
     * Настраивает конфигуратор REST API.
     *
     * @return настроенный RepositoryRestConfigurer
     */
    @Bean
    public RepositoryRestConfigurer repositoryRestConfigurer() {
        return new RepositoryRestConfigurer() {
            @Override
            public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
                // Включаем экспозицию ID в JSON-ответах
                config.exposeIdsFor(Author.class, Book.class, Category.class);

                // Устанавливаем базовый путь API
                config.setBasePath("/api");

                // Настраиваем разрешенные операции для сущностей
                ExposureConfiguration exposureConfig = config.getExposureConfiguration();

                // Для авторов запрещаем операции DELETE
                exposureConfig.forDomainType(Author.class)
                    .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

                // Для категорий запрещаем операции DELETE
                exposureConfig.forDomainType(Category.class)
                    .withItemExposure((metadata, httpMethods) ->
                        httpMethods.disable(HttpMethod.DELETE));

                // CORS конфигурация
                cors.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
            }
        };
    }
}