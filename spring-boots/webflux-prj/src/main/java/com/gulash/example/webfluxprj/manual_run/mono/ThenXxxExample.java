package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class ThenXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        then();
        thenMany();
        thenEmpty();
    }

    private static void then() {
        Mono<String> monoMain = Mono.just("Mono One")
            .doFirst(() -> System.out.println("Mono Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Mono Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<String> mono = Mono.just("Mono message")
            .doFirst(() -> System.out.println("Mono Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Mono Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        // todo then - выполняет Mono<T>(доп. действия), после основного потока, при этом НЕ используя результат предыдущей операции.
        Mono<String> then = monoMain.then(mono);

        Disposable disposable = then.subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            Mono Operation start
            Mono One
            Mono Operation completed
            Mono Operation start
            Mono message
            Mono Operation completed
         */
    }

    private static void thenMany() {
        Mono<String> monoMain = Mono.just("Mono Main")
            .doFirst(() -> System.out.println("FluxMain Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("FluxMain Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Flux<String> fluxOther = Flux.just("Flux Other One", "Flux Other Two")
            .doFirst(() -> System.out.println("FluxOther Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("FluxOther Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Disposable disposable = monoMain
            // todo thenMany - выполняет Mono<X>(доп. действия), после основного потока, при этом НЕ используя результат предыдущей операции.
            .thenMany(fluxOther).subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            FluxMain Operation start
            Mono Main
            FluxMain Operation completed
            FluxOther Operation start
            Flux Other One
            Flux Other Two
            FluxOther Operation completed
        */
    }

    private static void thenEmpty() {

        Mono<String> mono = Mono.just("Mono One")
            .doFirst(() -> System.out.println("Mono Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Mono Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<Void> voidMono = Mono
            .fromRunnable(() -> System.out.println("Mono_Void actions"));

        Mono<Void> thenEmpty = mono
            // todo thenEmpty - выполняет Mono<Void>(служебные действия), после основного потока, при этом НЕ используя результат предыдущей операции.
            .thenEmpty(voidMono);

        Disposable disposable = thenEmpty.subscribe();

        waitForDisposableEnd(List.of(disposable));
    /*
        Mono Operation start
        Mono One
        Mono Operation completed
        Mono_Void actions
    */
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
