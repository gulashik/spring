package com.gulash.example.webfluxprj.manual_run.mono.mono_to_flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FlatMapIterableExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        flatMapIterable();
    }

    private static void flatMapIterable() {
        Mono<String> mono = Mono.just("A B C");

        // todo flatMapIterable - преобразование элемента в Iterable, получаем Flux<X>
        Flux<String> flux = mono.flatMapIterable(value -> List.of(value.split(" ")));

        flux.subscribe(System.out::println);
        // A
        // B
        // C
    }
}
