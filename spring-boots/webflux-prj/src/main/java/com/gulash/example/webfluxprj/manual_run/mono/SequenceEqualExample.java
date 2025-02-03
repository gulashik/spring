package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class SequenceEqualExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<Integer> mono1 = Mono.just(1);
        Mono<Integer> mono2 = Mono.just(1);
        Flux<Integer> flux1 = Flux.just(1, 2, 3);
        Flux<Integer> flux2 = Flux.just(1, 2, 3);

        // todo Mono.sequenceEqual - одинаковый порядок и сами элементы
        Mono<Boolean> booleanMono = Mono.sequenceEqual(mono1, mono2);
        Mono<Boolean> booleanFlux = Mono.sequenceEqual(flux1, flux2);

        Boolean block1 = booleanMono.block();
        Boolean block2 = booleanFlux.block();

        System.out.println("Monos is equal: " + block1);
        System.out.println("Fluxes is equal: " + block2);
        /*
            Monos is equal: true
            Fluxes is equal: true
        */
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> { while (!disposable.isDisposed()) {}}
        );
    }
}
