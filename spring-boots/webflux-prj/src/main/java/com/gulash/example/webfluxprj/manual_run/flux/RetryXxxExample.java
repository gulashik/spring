package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        //retry();
        retryWhen();
    }

    private static void retry() {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        Disposable disposable = Flux
            // поток
            .range(10,5).delayElements(Duration.ofMillis(500))
            // регистрируем попытку, чтобы потом прервать(для демонстраци только)
            .doFirst(() -> System.out.println("********* attempt:" + atomicInteger.incrementAndGet()))

            .handle(
                (Integer i, SynchronousSink<String> sink) -> {
                    if ( i == 13 && atomicInteger.get() != 3 ) {
                        sink.error(new RuntimeException("Simulated error"));
                        // return;
                    }
                    sink.next("Value " + i);
                }
            )
            // todo retry - при ошибке ПОЛНАЯ re-эмиссия X-раз
            //.retry(/*default Long.MAX_VALUE*/)
            .retry(6)
            .doOnError(e -> System.out.println("All retries failed"))
            .subscribe(
                value -> System.out.println("Received: " + value),
                error -> System.out.println("Error: " + error.getMessage())
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            ********* attempt:1
            Received: Value 10
            Received: Value 11
            Received: Value 12
            ********* attempt:2
            Received: Value 10
            Received: Value 11
            Received: Value 12
            ********* attempt:3
            Received: Value 10
            Received: Value 11
            Received: Value 12
            Received: Value 13
            Received: Value 14
         */
    }

    private static void retryWhen() {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        Disposable disposable = Flux
            // поток
            .range(10,5).delayElements(Duration.ofMillis(500))
            // регистрируем попытку, чтобы потом прервать(для демонстраци только)
            .doFirst(() -> atomicInteger.incrementAndGet())
            .handle(
                (Integer i, SynchronousSink<String> sink) -> {
                    if ( i == 13 && atomicInteger.get() != 3 ) {
                        sink.error(new RuntimeException("Simulated error"));
                        // return;
                    }
                    sink.next("Value " + i);
                }
            )
            // todo retryWhen - при ошибке ПОЛНАЯ re-эмиссия X-раз c настройками
            .retryWhen(
                Retry
                    // todo backoff
                    .backoff(3, Duration.ofMillis(500)) // 3 попытки с экспоненциальной задержкой
                        .maxBackoff(Duration.ofMillis(5000)) // Максимальная задержка 5 секунд
                    // или
                    //.fixedDelay(3, Duration.ofSeconds(2)) // 3 попытки с задержкой 2 секунды

                    // todo фильтрация по классу ошибки
                    .filter(throwable -> throwable instanceof RuntimeException) // Повторить только при RuntimeException
                    // todo интересные методы
                    .doBeforeRetry(retrySignal -> System.out.println("********* attempt:" + retrySignal.totalRetries()))
            )
            .doOnError(e -> System.out.println("All retries failed"))
            .subscribe(
                value -> System.out.println("Received: " + value),
                error -> System.out.println("Error: " + error.getMessage())
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            Received: Value 10
            Received: Value 11
            Received: Value 12
            ********* attempt:0
            Received: Value 10
            Received: Value 11
            Received: Value 12
            ********* attempt:1
            Received: Value 10
            Received: Value 11
            Received: Value 12
            Received: Value 13
            Received: Value 14
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
