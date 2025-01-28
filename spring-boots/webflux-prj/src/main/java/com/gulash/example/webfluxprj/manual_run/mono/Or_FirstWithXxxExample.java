package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Or_FirstWithXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        {
            Supplier<Long> nextLong = () -> {
                long result = new Random().nextLong(100, 1000);
                System.out.println("result: " + result);
                return result;
            };
            Mono<String> source1 = Mono.just("Источник 1 данные получены").delayElement(Duration.ofMillis(nextLong.get()));
            Mono<String> source2 = Mono.just("Источник 2 данные получены").delayElement(Duration.ofMillis(nextLong.get()));
            Mono<String> source3 = Mono.just("Источник 3 данные получены").delayElement(Duration.ofMillis(nextLong.get()));

            // todo or - возвращает Mono<T>, содержащий элементы первого завершившегося Publisher.

            Mono<String> orMono =
                source1.doOnNext(s -> System.out.println("1 source is faster"))
                .or(
                    source2.doOnNext(s -> System.out.println("2 source is faster"))
                ).or(
                    source3.doOnNext(s -> System.out.println("3 source is faster"))
                );

            // todo firstWithValue - static метод возвращает Mono<T> первый поток с непустым результатом.
            Mono<String> mono1 = Mono.firstWithValue(
                source1.doOnNext(s -> System.out.println("1 source is faster")),
                source2.doOnNext(s -> System.out.println("2 source is faster")),
                source3.doOnNext(s -> System.out.println("3 source is faster"))
            );

            // todo firstWithSignal - static метод возвращает Mono<T> первый поток с выпустивший signal(value, empty completion or error).
            Mono<String> mono2 = Mono.firstWithSignal(
                source1.doOnNext(s -> System.out.println("1 source is faster")),
                source2.doOnNext(s -> System.out.println("2 source is faster")),
                source3.doOnNext(s -> System.out.println("3 source is faster"))
            );

            Disposable disposable = orMono
                .doFirst(() -> System.out.println("\nor start"))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    System.out::println,
                    throwable -> System.err.println("Ошибка: " + throwable),
                    () -> System.out.println("or Обработка завершена")
                );
            waitForDisposableEnd(List.of(disposable));

            Disposable disposable1 = mono1
                .doFirst(() -> System.out.println("\nfirstWithValue start"))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    System.out::println,
                    throwable -> System.err.println("Ошибка: " + throwable),
                    () -> System.out.println("firstWithValue Обработка завершена")
                );

            waitForDisposableEnd(List.of(disposable1));

            Disposable disposable2 = mono2
                .doFirst(() -> System.out.println("\nfirstWithSignal start"))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    System.out::println,
                    throwable -> System.err.println("Ошибка: " + throwable),
                    () -> System.out.println("firstWithSignal Обработка завершена")
                );

            waitForDisposableEnd(List.of(disposable2));
            /*
                or start
                3 source is faster
                Источник 3 данные получены
                or Обработка завершена

                firstWithValue start
                3 source is faster
                Источник 3 данные получены
                firstWithValue Обработка завершена

                firstWithSignal start
                3 source is faster
                Источник 3 данные получены
                firstWithValue Обработка завершена
            */
        }
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
