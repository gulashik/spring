package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class WhenXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        when();
        whenDelayError();
    }

    private static void when() {
        Flux<String> one = Flux
            //.<String>error(new RuntimeException("error"))
            .just("One","One","One")
            .delayElements(Duration.ofMillis(100))
            .doOnNext(System.out::println);
        Flux<String> two = Flux.just("Two","Two","Two")
            .delayElements(Duration.ofMillis(200))
            .doOnNext(System.out::println);
        Flux<String> three = Flux
            //.<String>error(new RuntimeException("error"))
            .just("Three","Three","Three")
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
            Three
            Three
            One
            Two
            One
            One
            Two
            Two
            All monos completed
            Actions after completion
            Going further
        */
    }
    private static void whenDelayError() {
        Flux<String> one = Flux
            //.<String>error(new RuntimeException("error"))
            .just("One", "One", "One")
            .delayElements(Duration.ofMillis(100))
            .doOnNext(System.out::println);
        Flux<String> two = Flux.just("Two", "Two", "Two")
            .delayElements(Duration.ofMillis(200))
            .doOnNext(System.out::println);
        Flux<String> three = Flux
            .<String>error(new RuntimeException("error"))
            //.just("Three", "Three", "Three")
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
            Three
            Three
            One
            Two
            One
            One
            Two
            Two
            All monos completed
            Actions after completion
            Going further

            -- с ошибкой
            One
            Two
            One
            One
            Two
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
