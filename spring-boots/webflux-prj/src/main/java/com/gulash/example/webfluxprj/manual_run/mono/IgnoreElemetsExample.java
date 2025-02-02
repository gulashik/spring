package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class IgnoreElemetsExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        // возвращает Mono<>
        Mono<String> mono = Mono
            // todo ignoreElements - отбрасывает элемент потока(дальше он не идут), возвращает Mono<>
            // todo ignoreElements - нужно просто дождаться завершения потока, не занимаясь обработкой данных
            .ignoreElements(
                Mono.just("main stream value").delayElement(Duration.ofSeconds(1))
            ) // todo Вернётся Mono empty
            .doOnNext(System.out::println) // ИГНОРИРУЕСЯ, т.к. будет empty Mono
            // todo then - Нужные действия после завершения потока
            .then(
                Mono.fromRunnable(
                    () -> System.out.println("Важен факт завершения потока. Тут служебные действия.")
                )
            );

        Disposable disposable = mono
            .subscribe(
                System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done")

            );
        waitForDisposableEnd(List.of(disposable));
        /*
            Важен факт завершения потока. Тут служебные действия.
            Done
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
