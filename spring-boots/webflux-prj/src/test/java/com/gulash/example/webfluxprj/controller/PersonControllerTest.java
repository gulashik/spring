package com.gulash.example.webfluxprj.controller;

import com.gulash.example.webfluxprj.BaseContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootTest
class PersonControllerTest extends BaseContainerTest {

    @Autowired
    private RouterFunction<ServerResponse> route;

    @Test
    void testRoute() {
        WebTestClient client = WebTestClient
                .bindToRouterFunction(route)
                .build();

        client.get()
                .uri("/func/person")
                .exchange()
                .expectStatus()
                .isOk();
    }
}
