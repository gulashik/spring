package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.publisher.Flux;

public class concatMapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        Flux.just(1, 2, 3)
            // todo concatMap обрабатывает каждый элемент как асинхронный поток.
            //  Он применяет функцию преобразования, которая возвращает новый Mono или Flux для каждого элемента.
            //  Эти потоки обрабатываются последовательно (один за другим).
            .concatMap(i -> simulateAsyncOperation(i)) // Асинхронная обработка
            .subscribe(System.out::println);

    }

    private static Flux<String> simulateAsyncOperation(int number) {
        return Flux.just("Processing " + number)
            .doOnNext(item -> simulateDelay(1000)); // Имитация задержки
    }

    private static void simulateDelay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
