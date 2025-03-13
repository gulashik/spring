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

    // todo запускаем compose.md
    public static void main( String[] args ) {
        SpringApplication.run( WebFluxSpringSecurity.class );

        System.out.println("""
            Переходим
            ->  http://localhost:8080/
            """);
    }

    @Bean
    RouterFunction<ServerResponse> staticResourceRouter() {
        return RouterFunctions.resources( "/**.html", new ClassPathResource( "static/" ) );
    }
}