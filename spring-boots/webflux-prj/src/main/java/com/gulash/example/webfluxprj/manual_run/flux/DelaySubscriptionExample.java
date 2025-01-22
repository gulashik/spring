package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DelaySubscriptionExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        delay();
    }

    private static void delay() {
        Flux<Integer> flux = Flux.range(1, 5)
            .doOnSubscribe((subscription) -> System.out.println("Подписка: " + LocalDateTime.now()))
            // todo delaySubscription - отложить выполнение подписки всего потока
            .delaySubscription(Duration.ofSeconds(1)/*, Schedulers.parallel() - Вычислительные задачи (по умолчанию)*/)
            .delaySubscription(Duration.ofSeconds(1), Schedulers.boundedElastic()/*Блокирующие или долгие задачи*/);

        System.out.println("Before call subscribe: " + LocalDateTime.now());
        Disposable disposable = flux.subscribe(i -> System.out.println(LocalDateTime.now() +  ": " + i));

        waitForDisposableEnd(List.of(disposable));
        /*
            Before call subscribe: 2025-01-22T17:45:27.615894
            Подписка: 2025-01-22T17:45:29.630169 Тут подписка отложена на две секунды
            2025-01-22T17:45:29.630382: 1
            2025-01-22T17:45:29.632253: 2
            2025-01-22T17:45:29.632301: 3
            2025-01-22T17:45:29.632321: 4
            2025-01-22T17:45:29.632338: 5
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
