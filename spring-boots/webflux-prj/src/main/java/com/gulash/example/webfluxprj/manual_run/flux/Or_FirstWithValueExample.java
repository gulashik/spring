package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Or_FirstWithValueExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Supplier<Long> nextLong = () -> {
            long result = new Random().nextLong(100, 1000);
            System.out.println("result: " + result);
            return result;
        };
        Flux<String> source1 = Flux.just("Источник 1 данные получены").delayElements(Duration.ofMillis(nextLong.get()));
        Flux<String> source2 = Flux.just("Источник 2 данные получены").delayElements(Duration.ofMillis(nextLong.get()));
        Flux<String> source3 = Flux.just("Источник 3 данные получены").delayElements(Duration.ofMillis(nextLong.get()));

        // todo or - возвращает Flux<T>, содержащий элементы первого завершившегося Publisher.
        Flux<String> flux =
            source1.doOnNext(s -> System.out.println("1 source is faster"))
                .or(
                    source2.doOnNext(s -> System.out.println("2 source is faster"))
                ).or(
                    source3.doOnNext(s -> System.out.println("3 source is faster"))
                );

        // todo firstWithValue - static метод возвращает Flux<T> первый поток с непустым результатом.
        Flux<String> flux1 = Flux.firstWithValue(
                source1.doOnNext(s -> System.out.println("1 source is faster")),
                source2.doOnNext(s -> System.out.println("2 source is faster")),
                source3.doOnNext(s -> System.out.println("3 source is faster"))
            );

        Disposable disposable = flux
            .doFirst(() -> System.out.println("\nor start"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                System.out::println,
                throwable -> System.err.println("Ошибка: " + throwable),
                () -> System.out.println("or Обработка завершена")
            );
        waitForDisposableEnd(List.of(disposable));

        Disposable disposable1 = flux1
            .doFirst(() -> System.out.println("\nfirstWithValue start"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                System.out::println,
                throwable -> System.err.println("Ошибка: " + throwable),
                () -> System.out.println("firstWithValue Обработка завершена")
            );

        waitForDisposableEnd(List.of(disposable1));
        /*
            result: 536
            result: 301
            result: 352

            or start
            2 source is faster
            Источник 2 данные получены
            or Обработка завершена

            firstWithValue start
            2 source is faster
            Источник 2 данные получены
            firstWithValue Обработка завершена
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
