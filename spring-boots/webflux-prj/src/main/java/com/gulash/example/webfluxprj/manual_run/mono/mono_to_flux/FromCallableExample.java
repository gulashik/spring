package com.gulash.example.webfluxprj.manual_run.mono.mono_to_flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FromCallableExample {
    public static void main(String[] args) {
        monoFromCallable();
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
