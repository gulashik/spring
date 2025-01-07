package com.gulash.example.webfluxprj.controller.manual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MonoGenerateController {

    // curl 'http://localhost:8080/demo/mono/just' --header 'Accept: */*' --header 'Content-Type: application/json' --header 'Cache-Control: no-cache'
    @GetMapping("/demo/mono/just")
    public Mono<String> one() {
        return Mono.just("one");
    }
}