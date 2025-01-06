package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;

import java.util.List;

public class DoXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        template();
    }

    private static void template() {
        Flux<Integer> flux = Flux.range(1, 10)
            // todo ==========================================================================
            // todo  doFirst - для подготовки перед выполнением реактивного потока.
            // todo  несколько doFirst, они выполняются в обратном порядке (LIFO — “последний пришел, первый выполнен”).
            .doFirst(() -> System.out.println("doFirst step"))

            // todo ==========================================================================
            // todo doOnSubscribe выполняется до начала выполнения потока, но после вызова метода subscribe.
            .doOnSubscribe(
                subscription -> {
                    System.out.println("doOnSubscribe step");
                }
            )

            // todo ==========================================================================
            // todo - ????
            //

            // todo ==========================================================================
            // todo doOnEach - получаем сигнал(содержит разные данные)
            .doOnEach(
                signal -> {
                    // todo сами данные
                    Integer i = signal.get();
                    System.out.println("doOnEach. current item: " + i);

                    // todo read-only context
                    String ctxValue = signal.getContextView().getOrDefault("key1", "no_key");
                    System.out.println("ctxValue: " + ctxValue);
                }
            )

            // todo ==========================================================================
            // todo doOnNext - получаем данные из сигнала
            .doOnNext(integer -> System.out.println("doOnNext. current item: " + integer))

            // todo ==========================================================================
            // todo doFinally - после завершения потока
            .doFinally(
                signalType -> {
                    System.out.println("doFinally. current state: " + signalType.name());
                }
            )
            ;

        flux.subscribe(
            integer -> System.out.println("from subscribe: " + integer),
            throwable -> { throw new RuntimeException("Some exception"); },
            () -> System.out.println("Done from subscribe"),
            Context.of("key1","Value1")
        );
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
