package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class TakeXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        //takeUntilOther();
        take();
    }

    private static void take() {

        Mono<String> dataStream = Mono
            .just("MAIN signal")
            .doFirst(() -> System.out.println("MAIN stream starts "))
            .delayElement(Duration.ofSeconds(5))
            // todo take - что случиться раньше 1 завершение основного потока или 2 timeout
            .take(Duration.ofSeconds(2)/*, Schedulers.parallel()*/)
            .switchIfEmpty(Mono.just("FALLBACK signal"));

        Disposable disposable = dataStream.subscribe(
            value -> System.out.println("Get in subscribe: " + value),
            error -> System.err.println("Exception in subscribe: " + error),
            () -> System.out.println("Completed in subscribe")
        );
        /*
            MAIN stream starts
            Get in subscribe: FALLBACK signal
            Completed in subscribe
        */
        waitForDisposableEnd(List.of(disposable));
    }

    private static void takeUntilOther() {

        Mono<String> dataStream = Mono
            .just("MAIN signal")
            .doFirst(() -> System.out.println("MAIN stream starts "))
            .delayElement(Duration.ofSeconds(5))
            // todo takeUntilOther - что случиться раньше 1 завершение основного потока или 2 эмиссия сигнала на завершение
            .takeUntilOther(
                Mono.delay(Duration.ofSeconds(2))
                    .doOnNext(aLong -> System.out.println("signal from takeUntilOther to complete MAIN stream")))
            .switchIfEmpty(Mono.just("FALLBACK signal"));

        Disposable disposable = dataStream.subscribe(
            value -> System.out.println("Get in subscribe: " + value),
            error -> System.err.println("Exception in subscribe: " + error),
            () -> System.out.println("Completed in subscribe")
        );
        /*
            MAIN stream starts
            signal from takeUntilOther to complete MAIN stream
            Get in subscribe: FALLBACK signal
            Completed in subscribe
        */
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
