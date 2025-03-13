package ru.otus.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
public class WebFluxSpringSecurity {

    // todo не запускается. Оставил как пример настройки
    public static void main( String[] args ) {
        SpringApplication.run( WebFluxSpringSecurity.class );
    }

    @Bean
    RouterFunction<ServerResponse> staticResourceRouter() {
        return RouterFunctions.resources( "/**.html", new ClassPathResource( "static/" ) );
    }
}
