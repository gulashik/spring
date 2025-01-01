package com.gulash.example.webfluxprj.manual_run.mono.mono_to_flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FlatMapManyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        flatMapMany();
    }

    private static void flatMapMany() {
        Mono<String> mono = Mono.just("Hello");

        // todo flatMapMany - используется для
        //  - преобразования одного элемента в поток (или последовательность) элементов
        //  - с асинхронными источниками данных
        Flux<String> flux = mono.flatMapMany(value -> Flux.just(value.split("")));

        flux.subscribe(System.out::println); // Вывод: H e l l o
    }

}
