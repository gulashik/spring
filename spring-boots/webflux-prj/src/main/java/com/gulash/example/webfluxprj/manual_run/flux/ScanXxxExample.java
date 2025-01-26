package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class ScanXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        scan();
        scanWith();
    }

    private static void scan() {
        Flux<Integer> scanned = Flux.range(1, 3)
            // todo scan - состояние с прошлого шага
            .scan(
                1, /*todo initial value*/
                (accum, currValue) -> {

                    System.out.println("accum = %s, currValue = %s".formatted(accum, currValue));
                    return accum + currValue; /*todo accumulator*/
                }
            );

        Disposable disposable = scanned
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(integer -> System.out.println("subscribed value: " + integer));

        waitForDisposableEnd(List.of(disposable));
        /*
            subscribed value: 1
            accum = 1, currValue = 1
            subscribed value: 2
            accum = 2, currValue = 2
            subscribed value: 4
            accum = 4, currValue = 3
            subscribed value: 7
        */
    }

    private static void scanWith() {
        Flux<Integer> scanned = Flux.range(1, 3)
            // todo scan - состояние с прошлого шага
            .scanWith(
                /*todo initial Supplier*/
                () -> 1,
                /*todo accumulator*/
                (accum, currValue) -> {

                    System.out.println("accum = %s, currValue = %s".formatted(accum, currValue));
                    return accum + currValue;
                }
            );

        Disposable disposable = scanned
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(integer -> System.out.println("subscribed value: " + integer));

        waitForDisposableEnd(List.of(disposable));
        /*
            subscribed value: 1
            accum = 1, currValue = 1
            subscribed value: 2
            accum = 2, currValue = 2
            subscribed value: 4
            accum = 4, currValue = 3
            subscribed value: 7
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
