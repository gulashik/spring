package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class ErrorMethodExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        //Lazy создание экземпляра
        Mono<String> lazyErrorFlux = Mono.<String>error(() -> new IllegalStateException("Lazy error"))
            .onErrorResume(throwable -> Mono.just("fallback value for Lazy error"));

        lazyErrorFlux.subscribe(
            System.out::println,
            error -> System.err.println("Error: " + error.getMessage())// Обработка ошибки
        ); // fallback value for Lazy error

        //Eager создание экземпляра
        Mono<String> eagerWithError = Mono.<String>error(new RuntimeException("An error occurred"))
            .onErrorResume(throwable -> Mono.just("fallback value for Eager error"));

        eagerWithError.subscribe(
            System.out::println, // Данные
            error -> System.err.println("Error: " + error.getMessage()), // Обработка ошибки
            () -> System.out.println("Completed") // Завершение
        ); // fallback value for Eager error
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
