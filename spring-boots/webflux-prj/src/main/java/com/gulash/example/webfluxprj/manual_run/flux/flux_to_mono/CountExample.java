package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class CountExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<String> flux = Flux.just("apple", "banana", "cherry");

        // todo count - количество элементов в потоке в Mono<Long>
        Mono<Long> countMono = flux.count();

        countMono.subscribe(count -> System.out.println("Count: " + count));

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
