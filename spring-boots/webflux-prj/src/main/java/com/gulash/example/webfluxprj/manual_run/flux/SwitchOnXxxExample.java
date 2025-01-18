package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SwitchOnXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        switchOnFirst();
    }

    private static void switchOnFirst() {
        Flux<Integer> fluxSwitched = Flux.range(1, 4)
            .publishOn(Schedulers.boundedElastic())
            // todo switchOnFirst - выполнить операцию ТОЛЬКО НА ПЕРВОМ элементе потока, и вернуть ИСХОДНЫЙ поток.
            .switchOnFirst(
                (   // todo сигнал от ПЕРВОГО ЭЛЕМЕНТА
                    Signal<? extends Integer> signal,
                    // todo  ИСХОДНЫЙ ПОТОК данных
                    Flux<Integer> fluxOriginal
                ) -> {
                    Integer i = signal.get();
                    System.out.println("first element = " + i + " Thread: " + Thread.currentThread().getName());

                    // todo можно без модификации, но если нужно можно добавить методов
                    return fluxOriginal
                        .publishOn(Schedulers.parallel())
                        .doOnNext(integer -> System.out.println("other elements = " + integer + " Thread: " + Thread.currentThread().getName()));

                },
                // todo cancelSourceOnComplete – specify whether original publisher should be cancelled on onComplete from the derived one
                true
            );

        Disposable disposable = fluxSwitched
            .subscribe(
                integer -> System.out.println("subscribe method: " + integer)
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            first element = 1 Thread: boundedElastic-1
            other elements = 1 Thread: parallel-1
            subscribe method: 1
            other elements = 2 Thread: parallel-1
            subscribe method: 2
            other elements = 3 Thread: parallel-1
            subscribe method: 3
            other elements = 4 Thread: parallel-1
            subscribe method: 4
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

