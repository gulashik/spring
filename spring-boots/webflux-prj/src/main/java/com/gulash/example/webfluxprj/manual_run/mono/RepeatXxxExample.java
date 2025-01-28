package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class RepeatXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        repeat();
        repeatWhen();
    }

    private static void repeat() {
        Flux<Integer> repeatFlux = Mono.just(9)
            // todo repeat - вычисление количество дополнительных ПОЛНЫХ re-эмиссий
            .repeat(
                // todo максимум эмиссий
                3,
                // todo или Predicate для повторной re-эмиссий
                () -> {
                    boolean b = new Random().nextBoolean();
                    System.out.println("now is " + b);
                    return b;
                }
            )
            //.repeat(2); // todo будет ТРИ эмиссии ОДНА штатная и ДВЕ re-эмиссии
            //.repeat() // todo БЕСКОНЕЧНОЕ ПОВТОРЕНИЕ
            ;
        /*
            9
            now is true
            9
            now is true
            9
            now is false
        */
        Disposable disposable = repeatFlux
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
    }

    private static void repeatWhen() {
        Flux<Integer> repeatFlux = Mono.just(9)
            // todo repeatWhen - функция для вычисления необходимости дополнительной ПОЛНОЙ re-эмиссии
            // основано на событиях, происходящих в другом потоке.
            .repeatWhen(
                // todo Flux<Long> (поток, который эмитит элементы с задержкой), где Long - порядковый индекс сообщения
                (Flux<Long> longFlux) -> {
                    // todo и должна вернуть Flux
                    return longFlux
                        .delayElements(Duration.ofMillis(500))
                        .doOnNext(aLong -> System.out.println("current emission count - %s; will be re-emission".formatted(aLong)))
                        .take(2);
                }
            );
        /*
            9
            current emission count - 1; will be re-emission
            9
            current emission count - 1; will be re-emission
            9
        */
        Disposable disposable = repeatFlux
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

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
