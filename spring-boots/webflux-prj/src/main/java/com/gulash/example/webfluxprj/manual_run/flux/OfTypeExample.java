package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class OfTypeExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Object> flux = Flux.just("Message 1", 42, "Message 2", new Exception("Error"), "Message 3", 12, "Message 4");
        // todo ofType - фильтрует элементы потока, которые соответствуют определенному типу.

        flux
            .doFirst(() -> System.out.println("Strings"))
            .ofType(String.class)
            .map(String::toUpperCase)
            .subscribe(System.out::println);
            /*
                Strings
                MESSAGE 1
                MESSAGE 2
                MESSAGE 3
                MESSAGE 4
            */
        flux
            .doFirst(() -> System.out.println("Numbers"))
            .ofType(Number.class)
            .subscribe(System.out::println);
            /*
                Numbers
                42
                12
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
