package com.gulash.example.webfluxprj.controller.manual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FluxGenerateController {

    private final Scheduler workerPool;

    // curl 'http://localhost:8080/demo/flux/range' --header 'Accept: */*' --header 'Content-Type: application/json' --header 'Cache-Control: no-cache'
    @GetMapping(path ="/demo/flux/range", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Integer> list() {
        log.info("list request");
        return Flux.range(1, 10)
            .delayElements(Duration.ofSeconds(1), workerPool)
            .doOnNext(val -> log.info("value:{}", val));
    }

    // curl 'http://localhost:8080/demo/flux/generate' --header 'Accept: */*' --header 'Content-Type: application/json' --header 'Cache-Control: no-cache'
    @GetMapping(path = "/demo/flux/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        log.info("stream");
        return Flux.generate(() -> 0, (state, emitter) -> {
                emitter.next(state);
                return state + 1;
            })
            .delayElements(Duration.ofSeconds(1L))
            .map(Object::toString)
            .map(val -> String.format("valStr:%s", val));
    }
}