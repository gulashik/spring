package com.gulash.example.webfluxprj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

// @EnableWebFlux // todo уже не нужна
@SpringBootApplication
public class WebfluxPrjApplication {

    // todo 1 запуск spring-boots/webflux-prj/compose.md
    // todo 2 руками можно позапускать из spring-boots/webflux-prj/src/main/java/com/gulash/example/webfluxprj/controller/manual
    public static void main(String[] args) {
        SpringApplication.run(WebfluxPrjApplication.class, args);
    }
}
