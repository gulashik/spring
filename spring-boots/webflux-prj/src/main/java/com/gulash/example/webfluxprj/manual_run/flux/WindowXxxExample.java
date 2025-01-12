package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class WindowXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        //window();
        //windowTimeout();
        //windowUntil();
        //windowUntilChanged();
        //windowWhile();
        windowWhen();
    }

    // todo WINDOW возвращает Flux<Flux<X>> в отличие BUFER Flux<List<X>>

    private static void window() {

        Flux<Flux<Integer>> window = Flux.range(1, 10)
            .subscribeOn(Schedulers.boundedElastic())
            // todo window - Создаёт Flux окна фиксированного размера.
            .window(3); // todo Разбивает поток на окна по 3 элемента

        Disposable disposable = window
            .flatMap( (Flux<Integer> flux) -> flux.collectList()) // todo Конвертируем каждое окно в список
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            [1, 2, 3]
            [4, 5, 6]
            [7, 8, 9]
            [10]
        */
    }

    private static void windowTimeout() {

        Flux<Flux<Long>> fluxFlux = Flux.interval(Duration.ofMillis(200))
            .take(10)
            // todo windowTimeout - Создаёт окна фиксированного размера ИЛИ с таймаутом.
            .windowTimeout(3, Duration.ofMillis(500));// todo Либо 3 элемента, либо 500 мс

        Disposable disposable = fluxFlux.flatMap((Flux<Long> flux) -> flux.collectList())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            [0, 1]
            [2, 3]
            [4, 5, 6]
            [7, 8]
            [9]
        */
    }

    private static void windowUntil() {

        Flux<Flux<Integer>> fluxFlux = Flux.range(1, 10)
            .doFirst(() -> System.out.println("Закрывает окно, когда элемент делится на 4"))
            // todo windowUntil - Создаёт окна, закрывающиеся при выполнении заданного условия.
            .windowUntil(i -> i % 4 == 0); // todo Закрывает окно, когда элемент делится на 4

        Disposable disposable = fluxFlux
            .flatMap((Flux<Integer> flux) -> flux.collectList())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Закрывает окно, когда элемент делится на 4
            [1, 2, 3, 4]
            [5, 6, 7, 8]
            [9, 10]
        */
    }

    private static void windowUntilChanged() {
    try {
        Flux<Flux<String>> fluxFlux = Flux.just("a", "a", "b", "b", "b", "c", "c", "d")
            .doFirst(() -> System.out.println("Закрывает окно при смене значения"))
            // todo windowUntilChanged - Создаёт окна, закрывающиеся при изменении значения.
            .windowUntilChanged(); // todo Закрывает окно при смене значения

        Disposable disposable = fluxFlux.flatMap(flux -> flux.collectList())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Закрывает окно при смене значения
            [a, a]
            [b, b, b]
            [c, c]
            [d]
        */
    } finally {}

    try {
        Flux<Flux<Integer>> fluxFlux = Flux.range(1,10)
            .doFirst(() -> System.out.println("Закрывает окно при смене значения ключа"))
            // todo windowUntilChanged - Создаёт окна, закрывающиеся при изменении значения ключа.
            .windowUntilChanged(
                i -> {
                    //System.out.println(i / 4);
                    return i / 4;
                }
            );
            /*
                [1, 2, 3]
                [4, 5, 6, 7]
                [8, 9, 10]
            */
        Disposable disposable = fluxFlux.flatMap(flux -> flux.collectList())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
    } finally {}
    }

    private static void windowWhile() {
        Flux<Flux<Integer>> fluxFlux = Flux.range(1, 10)
            .doFirst(() -> System.out.println("Создаёт окна, пока выполняется условие."))
            // todo windowWhile - Создаёт окна, пока выполняется условие.
            .windowWhile(i -> i < 5); // todo Создаёт окно, пока элементы < 5

        fluxFlux
            .flatMap(flux -> flux.collectList())
            .subscribe(System.out::println);
            /*
                [1, 2, 3, 4]
                []
                []
                []
                []
                []
            */
    }

    private static void windowWhen() {
        Flux<Long> triggerOpen = Flux.interval(Duration.ofMillis(500)).take(5);
        Flux<Long> triggerClose = Flux.interval(Duration.ofMillis(700)).take(5);

        Disposable disposable = Flux.interval(Duration.ofMillis(200))
            .take(100)
            // todo windowWhen - Создаёт окна, управляемые другими потоками (триггерами).
            .windowWhen(triggerOpen, t -> triggerClose) // Окно открывается и закрывается по событию
            //.windowWhen(triggerOpen, t -> Mono.just("go").delayElement(Duration.ofMillis(500)))
            .flatMap(flux -> flux.collectList())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));

        /*
            [2, 3, 4, 5]
            [5, 6, 7]
            [7, 8, 9, 10]
            [9, 10, 11]
            []
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