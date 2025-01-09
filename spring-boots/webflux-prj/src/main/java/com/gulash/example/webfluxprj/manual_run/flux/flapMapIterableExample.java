package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class flapMapIterableExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        flatMapAndflapMapIterable();
    }

    private static void flatMapAndflapMapIterable() {
        Flux<List<Integer>> listFlux = Flux.just(List.of(1, 2, 3), List.of(11, 22, 32));

        // todo Пример flatMapIterable
        listFlux
            .doFirst(() -> System.out.println("flatMapIterable example"))
            .flatMapIterable(list -> list) // todo Из Flux<List<Integer>> в Flux<Integer>
            .doOnNext(System.out::println)
            .subscribe()
        ;

        // todo Пример flatMap + fromIterable
        listFlux
            .doFirst(() -> System.out.println("flatMap + fromIterable example"))
            .flatMap(Flux::fromIterable) // todo Из Flux<List<Integer>> в Flux<Integer>
            .doOnNext(System.out::println)
            .subscribe()
        ;
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
