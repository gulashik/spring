package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class ThenXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        then();
        thenEmpty();
        thenMany();
    }

    private static void thenMany() {
        Flux<String> fluxMain = Flux.just("FluxMain One", "FluxMain Two", "FluxMain Three")
            .doFirst(() -> System.out.println("FluxMain Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("FluxMain Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Flux<String> fluxOther = Flux.just("FluxOther One", "FluxOther Two", "FluxOther Three")
            .doFirst(() -> System.out.println("FluxOther Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("FluxOther Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Disposable disposable = fluxMain
            // todo thenMany - выполняет Flux<X>(доп. действия), после основного потока, при этом не используя результат предыдущей операции.
            .thenMany(fluxOther).subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            FluxMain Operation start
            FluxMain One
            FluxMain Two
            FluxMain Three
            FluxMain Operation completed
            FluxOther Operation start
            FluxOther One
            FluxOther Two
            FluxOther Three
            FluxOther Operation completed
        */
    }

    private static void thenEmpty() {

        Flux<String> flux = Flux.just("Flux One", "Flux Two", "Flux Three")
            .doFirst(() -> System.out.println("Flux Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Flux Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<Void> voidMono = Mono
            .fromRunnable(() -> System.out.println("Mono_Void actions"));

        Mono<Void> thenEmpty = flux
            // todo thenEmpty - выполняет Mono<Void>(служебные действия), после основного потока, при этом не используя результат предыдущей операции.
            .thenEmpty(voidMono);

        Disposable disposable = thenEmpty.subscribe();

        waitForDisposableEnd(List.of(disposable));
    /*
        Flux Operation start
        Flux One
        Flux Two
        Flux Three
        Flux Operation completed
        Mono_Void actions
    */
    }

    private static void then() {
        Flux<String> flux = Flux.just("Flux One", "Flux Two", "Flux Three")
            .doFirst(() -> System.out.println("Flux Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Flux Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<String> mono = Mono.just("Mono message")
            .doFirst(() -> System.out.println("Mono Operation start"))
            .doOnNext(System.out::println)
            .doOnTerminate(() -> System.out.println("Mono Operation completed"))
            .subscribeOn(Schedulers.boundedElastic());

        // todo then - выполняет Mono<T>, после основного потока, при этом не используя результат предыдущей операции.
        Mono<String> then = flux.then(mono);

        Disposable disposable = then.subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            Flux Operation start
            Flux One
            Flux Two
            Flux Three
            Flux Operation completed
            Mono Operation start
            Mono message
            Mono Operation completed
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
