package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DealyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<Long> mono = Mono
            // todo delay - задержка эмисси элемента Long(содержит НОЛЬ)
            .delay(Duration.ofSeconds(1), Schedulers.boundedElastic())
            //.delay(Duration.ofSeconds(1)/* todo default, Schedulers.parallel()*/)
            .log();

        Disposable disposable = mono
            .doOnNext(value -> System.out.println("doOnNext: %s in %s%n".formatted(value, LocalDateTime.now())))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
        // data from stream
        waitForDisposableEnd(List.of(disposable));
            /* видим задержку в 2 секунды
            14:41:51.741 [boundedElastic-1] INFO reactor.Mono.Delay.1 -- onSubscribe(MonoDelay.MonoDelayRunnable)
            14:41:51.743 [boundedElastic-1] INFO reactor.Mono.Delay.1 -- request(unbounded)
            14:41:52.749 [parallel-1] INFO reactor.Mono.Delay.1 -- onNext(0)
            doOnNext: 0 in 2025-02-02T14:41:52.749243
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
