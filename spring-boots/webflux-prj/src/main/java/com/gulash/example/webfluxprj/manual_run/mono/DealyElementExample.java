package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DealyElementExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> mono = Mono.just("data from stream")
            .log()
            // todo delayElement - задержка эмисси элемента
            .delayElement(Duration.ofSeconds(1), Schedulers.boundedElastic())
            .delayElement(Duration.ofSeconds(1)/* todo default, Schedulers.parallel()*/);

        Disposable disposable = mono
            .doOnNext(value -> System.out.println("doOnNext: %s in %s%n".formatted(value, LocalDateTime.now())))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
        // data from stream
        waitForDisposableEnd(List.of(disposable));
        /* видим задержку в 2 секунды
            14:37:54.273 [boundedElastic-1] INFO reactor.Mono.Just.1 -- | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
            14:37:54.274 [boundedElastic-1] INFO reactor.Mono.Just.1 -- | request(unbounded)
            14:37:54.274 [boundedElastic-1] INFO reactor.Mono.Just.1 -- | onNext(data from stream)
            14:37:54.275 [boundedElastic-1] INFO reactor.Mono.Just.1 -- | onComplete()
            doOnNext: data from stream in 2025-02-02T14:37:56.285081
            // Видим 14:37:54 и 14:37:56
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
