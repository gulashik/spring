package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ToFutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() throws ExecutionException, InterruptedException {
        Mono<String> mono = Mono.
            just("Hello, Future with Executor!")
            .delayElement(Duration.ofMillis(1000));

        // Преобразуем Mono в Future с использованием Executor
        Future<String> future = mono.toFuture();

        // Блокируем и получаем результат
        System.out.println(future.get());
        // Output: Hello, Future with Executor!

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
