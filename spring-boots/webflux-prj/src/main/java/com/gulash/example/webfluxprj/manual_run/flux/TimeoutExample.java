package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.observability.SignalListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TimeoutExample {
    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() throws InterruptedException {
        Flux<String> flux = Flux.just("one main", "two main", "three main")
            .delayElements(Duration.ofMillis(100))
            // todo timeout - если по timeout нет первого элемента переключаемся на fallback поток
            .timeout(
                Duration.ofMillis(90)
                ,/*todo Опционально*/Flux.just("one fallback", "two fallback", "three fallback") // fallback - что будет вызвано при timeout
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
            two fallback
            three fallback
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
