package com.gulash.example.webfluxprj.manual_run.flux.init;

import reactor.core.publisher.Flux;

public class EmptyExample {
    public static void main(String[] args) {
        // empty() - пустой поток
        Flux.empty()
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
