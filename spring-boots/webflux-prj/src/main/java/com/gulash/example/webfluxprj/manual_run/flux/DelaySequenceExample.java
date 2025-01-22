package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Timed;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DelaySequenceExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        delay();
    }

    private static void delay() {
        Flux<Integer> flux = Flux.range(1, 5)
            .doFirst(() -> System.out.println("Начало: " + LocalDateTime.now()))
            // todo delaySequence - отложить выполнение всего потока
            .delaySequence(Duration.ofSeconds(1)/*, Schedulers.parallel() - Вычислительные задачи (по умолчанию)*/)
            .delaySequence(Duration.ofSeconds(1), Schedulers.boundedElastic()/*Блокирующие или долгие задачи*/);

        Disposable disposable = flux.subscribe(i -> System.out.println(LocalDateTime.now() +  ": " + i));

        waitForDisposableEnd(List.of(disposable));
        /*
            Начало: 2025-01-22T17:26:12.412553
            2025-01-22T17:26:14.419914: 1 вот тут видим что 2 секунды прошло
            2025-01-22T17:26:14.421997: 2
            2025-01-22T17:26:14.422031: 3
            2025-01-22T17:26:14.422060: 4
            2025-01-22T17:26:14.422086: 5
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
