package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class TimeoutExample {
    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() throws InterruptedException {
        Mono<String> flux = Mono.just("one main")
            .delayElement(Duration.ofMillis(100))
            // todo timeout - если по timeout нет первого элемента переключаемся на fallback поток
            .timeout(
                Duration.ofMillis(90)
                ,/*todo Опционально*/Mono.just("one fallback") // fallback - что будет вызвано при timeout
                // todo если не будет fallback то будет ошибка
                // todo java.util.concurrent.TimeoutException
            );

        Disposable disposable = flux
            .subscribe(
                System.out::println,
                Throwable::printStackTrace // действия если ошибка
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            one fallback
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
