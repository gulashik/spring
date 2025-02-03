package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class OfTypeExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<Object> mono = Mono.just(new Exception("Error"));
        // todo ofType - фильтрует элементы потока, которые соответствуют определенному типу.

        mono
            .doFirst(() -> System.out.println("Strings"))
            .ofType(String.class)
            .defaultIfEmpty("default value")
            .map(String::toUpperCase)
            .subscribe(System.out::println);
            /*
                Strings
                DEFAULT VALUE
            */
        Mono.just(1)
            .doFirst(() -> System.out.println("Numbers"))
            .ofType(Number.class)
            .defaultIfEmpty(0)
            .subscribe(System.out::println);
            /*
                Numbers
                1
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