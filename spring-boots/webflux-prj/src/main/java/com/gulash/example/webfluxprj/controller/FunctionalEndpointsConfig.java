package com.gulash.example.webfluxprj.controller;

import com.gulash.example.webfluxprj.model.Person;
import com.gulash.example.webfluxprj.repository.PersonRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Configuration
public class FunctionalEndpointsConfig {
    @Bean
    public RouterFunction<ServerResponse> composedRoutes(PersonRepo repository) {
        return route()
            // эта функция должна стоять раньше findAll - порядок следования роутов - важен
            .GET("/func/person",
                queryParam("name", StringUtils::isNotEmpty),
                request -> request.queryParam("name")
                    .map(name -> ok().body(repository.findAllByLastName(name), Person.class))
                    .orElse(badRequest().build())
            )
            // пример другой реализации - начиная с запроса репозитория
            .GET("/func/person", queryParam("age", StringUtils::isNotEmpty),
                request ->
                    ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(repository.findAllByAge(request.queryParam("age")
                            .map(Integer::parseInt)
                            .orElseThrow(IllegalArgumentException::new)), Person.class)
            )
            // Обратите внимание на использование хэндлера
            .GET("/func/person", accept(APPLICATION_JSON), new PersonHandler(repository)::list)
            // Обратите внимание на использование pathVariable
            .GET("/func/person/{id}", accept(APPLICATION_JSON),
                request -> repository.findById(Long.parseLong(request.pathVariable("id")))
                    .flatMap(person -> ok().contentType(APPLICATION_JSON).body(fromValue(person)))
                    .switchIfEmpty(notFound().build())
            ).build();
    }

    // Это пример хэндлера, который даже не бин
    static class PersonHandler {

        private final PersonRepo repository;

        PersonHandler(PersonRepo repository) {
            this.repository = repository;
        }

        Mono<ServerResponse> list(ServerRequest request) {
            // Обратите внимание на пример другого порядка создания response от Flux
            return ok().contentType(APPLICATION_JSON).body(repository.findAll(), Person.class);
        }
    }
}