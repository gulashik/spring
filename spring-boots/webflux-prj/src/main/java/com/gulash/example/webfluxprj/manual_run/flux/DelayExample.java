package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class DelayExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        delay();
    }

    private static void delay() {
        Disposable disposable = Flux.range(1, 5)
            .delayElements(Duration.ofSeconds(1)/*, Schedulers.parallel() - Вычислительные задачи (по умолчанию)*/)
            .delayElements(Duration.ofSeconds(1), Schedulers.boundedElastic()/*Блокирующие или долгие задачи*/)
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
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
