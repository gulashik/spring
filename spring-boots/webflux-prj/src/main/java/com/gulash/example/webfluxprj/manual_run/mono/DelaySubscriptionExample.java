package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        Mono<Integer> mono = Mono.just(1)
            .doOnSubscribe((subscription) -> System.out.println("Подписка: " + LocalDateTime.now()))
            // todo delaySubscription - отложить выполнение подписки всего потока
            .delaySubscription(Duration.ofSeconds(1)/*, Schedulers.parallel() - Вычислительные задачи (по умолчанию)*/)
            .delaySubscription(Duration.ofSeconds(1), Schedulers.boundedElastic()/*Блокирующие или долгие задачи*/);

        System.out.println("Before call subscribe: " + LocalDateTime.now());
        Disposable disposable = mono.subscribe(i -> System.out.println(LocalDateTime.now() +  ": " + i));

        waitForDisposableEnd(List.of(disposable));
        /*
            Before call subscribe: 2025-02-02T15:50:42.972939
            Подписка: 2025-02-02T15:50:44.984893
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
