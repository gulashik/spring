package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FromXxxxExample {
    public static void main(String[] args) {
        monoFrom();
        monoFromCallable();
    }

    private static void monoFrom() {
        Flux<Integer> otherPublisher = Flux.range(1, 10);

        // todo Mono.from() используется для создания Mono из источника, который является другим Publisher
        Mono.from(otherPublisher).subscribe(System.out::println);
        // 1
    }

    private static void monoFromCallable() {
        // todo Mono.fromCallable - получаем результат из Callable
        //  Можно использовать Mono.fromCallable с конвертацией во Flux
        Mono<List<Integer>> mono = Mono.fromCallable(
            () -> {
                // Синхронная операция
                return List.of(1, 2, 3, 4);
            }
        );

        mono
            .flux() // todo Mono into Flux<List<Integer>>
            .flatMapIterable(lst -> lst) // todo into Flux<Integers>
            .subscribe(
                result -> System.out.println("Received: " + result),
                error -> System.err.println("Error: " + error)
            );
    }
}
