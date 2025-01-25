package com.gulash.example.webfluxprj.manual_run.flux;

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
        Mono<Integer> mono = Flux.range(1, 5)
            .delayElements(Duration.ofMillis(500))
            .doOnNext(integer -> System.out.println("выше по потоку " + integer)) // НЕ игнорируется - буду напечатаны
            // todo ignoreElements - отбрасывает элемент потока(дальше он не идут), возвращает Mono<>
            // todo ignoreElements - нужно просто дождаться завершения потока, не занимаясь обработкой данных
            .ignoreElements() // todo Вернётся Mono empty
            .doOnNext(System.out::println); // ИГНОРИРУЕСЯ, т.к. будет empty Mono

        Disposable disposable = mono
            .subscribe(
                System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Done")

            );
        waitForDisposableEnd(List.of(disposable));
        /*
            выше по потоку 1
            выше по потоку 2
            выше по потоку 3
            выше по потоку 4
            выше по потоку 5
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
