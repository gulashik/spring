package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class StartWithExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Integer> range = Flux.range(11, 3)
            .doFirst(() -> System.out.println("range start"));

        Flux<Integer> flux1 = range
            // todo startWith - позволяет добавить элемент в начало потока.
            .startWith(10)
            .doFirst(() -> System.out.println("with startWith start"))
            .subscribeOn(Schedulers.boundedElastic())
            .doFinally(signalType -> System.out.println("---"));

        Disposable disposable1 = flux1.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposable1));
        /*
            with startWith start
            10
            range start
            11
            12
            13
            ---
        */

        Flux<Integer> flux2 = range
            // todo startWith - позволяет добавить коллекцию в начало потока.
            .startWith(List.of(1,2,3))
            .doFirst(() -> System.out.println("with startWith start"))
            .subscribeOn(Schedulers.boundedElastic())
            .doFinally(signalType -> System.out.println("---"))
            ;

        Disposable disposable2 = flux2.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposable2));
        /*
            with startWith start
            1
            2
            3
            range start
            11
            12
            13
            ---
        */

        Flux<Integer> flux3 = range
            // todo startWith - позволяет Publisher коллекцию в начало потока.
            .startWith(Flux.just(1,2,3))
            .doFirst(() -> System.out.println("with startWith start"))
            .subscribeOn(Schedulers.boundedElastic())
            .doFinally(signalType -> System.out.println("---"));

        Disposable disposable3 = flux3.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposable3));
        /*
            with startWith start
            1
            2
            3
            range start
            11
            12
            13
            ---
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
