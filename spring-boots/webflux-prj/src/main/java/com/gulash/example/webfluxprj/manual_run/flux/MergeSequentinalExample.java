package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MergeSequentinalExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        mergerExample();
    }

    private static void mergerExample() {
        Flux<String> flux1 = Flux.range(1, 10)
            .log("flux1")
            .flatMap(integer -> Mono.just(integer.toString()))
            .delayElements(Duration.of(100, ChronoUnit.MILLIS), Schedulers.boundedElastic());

        Flux<String> flux2 = Flux.just("one", "two", "three")
            .log("flux2")
            .delayElements(Duration.of(200, ChronoUnit.MILLIS), Schedulers.boundedElastic());

        // todo mergeSequential - гибрид между merge и concat: обработка выполняется асинхронно, но порядок потоков строго соблюдается..
        Disposable disposable = Flux.mergeSequential(flux1, flux2, flux1)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                System.out::println,
                RuntimeException::new,
                System.err::println
            );

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
