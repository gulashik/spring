package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class AndExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> mono = Mono.<String>empty()//.just("signal")
            .delayElement(Duration.ofMillis(500))
            .doOnNext(System.out::println);

        // todo hasElement - возвращает true/false в зависимости от того есть ли эелменты
        Mono<Boolean> booleanMono = mono.hasElement();

        Disposable disposable = booleanMono.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> {
                while (!disposable.isDisposed()) {
                }
            }
        );
    }
}
