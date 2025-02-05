package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class WhenXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        when();
        //whenDelayError();
    }

    private static void when() {
        Mono<String> one = Mono
            //.<String>error(new RuntimeException("error"))
            .just("One")
            .delayElement(Duration.ofMillis(100))
            .doOnNext(System.out::println);
        Mono<String> two = Mono.just("Two")
            .delayElement(Duration.ofMillis(200))
            .doOnNext(System.out::println);
        Mono<String> three = Mono
            //.<String>error(new RuntimeException("error"))
            .just("Three")
            .doOnNext(System.out::println);

        //  todo получаем Mono<Void>
        Mono<Void> when =
            // todo when - ожидаем завершения параллельного выполнения всех Publisher.
            //  ВАЖНО! Получаем Mono<Void>, т.е. метод subscribe() ничего не выполняет.
            //  Если любой из Publisher завершается с ошибкой, то и результирующий Mono СРАЗУ завершается С ОШИБКОЙ.
            Mono.when(one, two, three)
                .doOnSuccess(v -> System.out.println("All monos completed")) // не будет выполнен при ошибке
                .then(Mono.fromRunnable(() -> System.out.println("Actions after completion")))
            ;

        Disposable disposable = when
            .subscribe();

        waitForDisposableEnd(List.of(disposable));

        System.out.println("Going further"); // БУДЕТ выполнен при ошибке
        /*
            Three
            One
            Two
            All monos completed
            Actions after completion
            Going further
        */
    }
    private static void whenDelayError() {
        Mono<String> one = Mono
            //.<String>error(new RuntimeException("error"))
            .just("One")
            .delayElement(Duration.ofMillis(100))
            .doOnNext(System.out::println);
        Mono<String> two = Mono.just("Two")
            .delayElement(Duration.ofMillis(200))
            .doOnNext(System.out::println);
        Mono<String> three = Mono
            .<String>error(new RuntimeException("error"))
            //.just("Three")
            .doOnNext(System.out::println);

        //  todo получаем Mono<Void>
        Mono<Void> whenDelayError =
            // todo whenDelayError - ожидаем завершения параллельного выполнения всех Publisher.
            //  ВАЖНО! Получаем Mono<Void>, т.е. метод subscribe() ничего не выполняет.
            //  Если любой из Publisher завершается с ошибкой, то и результирующий Mono завершается БЕЗ ПАДЕНИЯ ПОСЛЕ ЗАВЕРШЕНИЯ всех Publisher.
            Mono.whenDelayError(one, two, three)
                .doOnSuccess(v -> System.out.println("All monos completed")) // не будет выполнен при ошибке
                .then(Mono.fromRunnable(() -> System.out.println("Actions after completion"))) // не будет выполнен при ошибке
            ;

        Disposable disposable = whenDelayError.subscribe();
        waitForDisposableEnd(List.of(disposable));
        System.out.println("Going further"); // БУДЕТ выполнен при ошибке
        /*  -- без ошибки
            Three
            One
            Two
            All monos completed
            Actions after completion
            Going further

            -- с ошибкой
            One
            Two
            Going further
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
