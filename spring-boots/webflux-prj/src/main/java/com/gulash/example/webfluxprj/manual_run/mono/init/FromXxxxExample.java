package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FromXxxxExample {
    public static void main(String[] args) {
        //monoFrom();
        //monoFromCallable();
        //monoFromCompletionStage();
        monoFromDirect();
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

    private static void monoFromCompletionStage() {

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello, from CompletableFuture");

        Mono<String> mono = Mono
            // todo fromCompletionStage - оборачивает CompletableFuture в Mono
            .fromCompletionStage(future);

        mono.subscribe(System.out::println);  // Hello, from CompletableFuture
    }

    private static void monoFromDirect() {

        Flux<String> source = Flux.just("one", "two", "three");

        Mono<String> mono = Mono
            // todo fromDirect - преобразует Publisher Mono, использует только первое событие
            .fromDirect(source.doFirst(() -> System.out.println("Get only first event")));

        mono.subscribe(System.out::println);  // one
    }
}
