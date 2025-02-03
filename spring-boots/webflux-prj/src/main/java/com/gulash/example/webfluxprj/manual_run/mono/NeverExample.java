package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class NeverExample {
    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() throws InterruptedException {
        Mono<String> neverMono =
            // todo never - никогда не завершится
            Mono.never();

        Disposable disposable = neverMono.subscribe(
            value -> System.out.println("Received: " + value), // Никогда не выполнится
            error -> System.err.println("Error: " + error),    // Никогда не выполнится
            () -> System.out.println("Completed")              // Никогда не выполнится
        );

        Thread.sleep(5000);
        disposable.dispose(); // чтобы завершиться

        //waitForDisposableEnd(List.of(disposable));
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
