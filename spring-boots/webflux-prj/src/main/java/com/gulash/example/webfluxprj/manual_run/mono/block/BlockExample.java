package com.gulash.example.webfluxprj.manual_run.mono.block;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class BlockExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        mergerExample();
    }

    private static void mergerExample() {

        /*
            НЕ ИСПОЛЬЗУЕМ subscribe

            Применение:
                Применяется в тестах.
                Используется в местах, где требуется интеграция реактивного кода с императивным.
        */
        Mono<String> mono = Mono
            .just("Hello, WebFlux!" + Thread.currentThread().getName())
            .log()
            .delayElement(Duration.ofSeconds(1),Schedulers.parallel())
            ;

        // todo block - Блокирует выполнение текущего потока до получения результата

        // todo Блокирует выполнение и возвращает String "Hello, WebFlux!"
        String result = mono.block();

        // todo Блокирует выполнение и возвращает String "Hello, WebFlux! или Exception по timeout"
        String resultDuration = mono.block(Duration.of(2, ChronoUnit.SECONDS));

        // todo Блокирует выполнение и возвращает Optional<String> c "Hello, WebFlux!"
        Optional<String> optionalResult = mono.blockOptional();

        System.out.println("-------------");
        System.out.println(result);
        System.out.println(resultDuration);
        System.out.println(optionalResult);
    }
}
