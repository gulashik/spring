package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.publisher.Mono;

public class EmptyExample {
    public static void main(String[] args) {
        // пустой поток
        Mono.empty()
            .doFirst(() -> System.out.println("Started"))
            .doOnNext(object -> System.out.println(object))
            .doFinally(signalType -> System.out.println(signalType))
            .subscribe();

        /* Результат
            Started
            onComplete
        */
    }
}
