package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
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

        Disposable disposable = Mono.just(13).delayElement(Duration.ofMillis(500))
            // регистрируем попытку, чтобы потом прервать(для демонстраци только)
            .doFirst(() -> System.out.println("********* attempt:" + atomicInteger.incrementAndGet()))

            .handle(
                (Integer i, SynchronousSink<String> sink) -> {
                    if (i == 13 && atomicInteger.get() != 3) {
                        System.out.println("Error occurred");
                        sink.error(new RuntimeException("Simulated error"));
                        return;
                    }
                    System.out.println("Success way occurred");
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
            Error occurred
            ********* attempt:2
            Error occurred
            ********* attempt:3
            Success way occurred
            Received: Value 13
         */

    }

    private static void retryWhen() {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        Disposable disposable = Mono.just(13).delayElement(Duration.ofMillis(500))
            // регистрируем попытку, чтобы потом прервать(для демонстраци только)
            .doFirst(() -> atomicInteger.incrementAndGet())
            .handle(
                (Integer i, SynchronousSink<String> sink) -> {
                    if (i == 13 && atomicInteger.get() != 3) {
                        System.out.println("Error occurred");
                        sink.error(new RuntimeException("Simulated error"));
                        return;
                    }
                    System.out.println("Success way occurred");
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
            Error occurred
            ********* attempt:0
            Error occurred
            ********* attempt:1
            Success way occurred
            Received: Value 13
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
