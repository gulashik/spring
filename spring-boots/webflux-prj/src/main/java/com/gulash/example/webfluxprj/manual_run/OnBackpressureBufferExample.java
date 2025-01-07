package com.gulash.example.webfluxprj.manual_run;

import reactor.core.Disposable;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class OnBackpressureBufferExample {
    public static void main(String[] args) {
        // todo onBackpressureBuffer - настройки хранения событий,для управления при избыточном количестве не обработанных событий

        //exampleWithTtll();
        exampleWithStrategy();

    }

    private static void exampleWithStrategy() {
        Disposable disposable = Flux.range(1, 100)
            .log()
            .onBackpressureBuffer(
                3, // todo храним максимум
                integer -> System.out.println("Dropped - " + integer), // todo Обработка переполнения
                BufferOverflowStrategy.DROP_OLDEST // todo стратегия переполнения
            )
            .delayElements(Duration.of(1, ChronoUnit.SECONDS), Schedulers.boundedElastic())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                System.out::println,
                error -> System.err.println("Error: " + error.getMessage())
            );

        waitForDisposableEnd(List.of(disposable));
    }
    private static void exampleWithTtll() {
        Disposable disposable = Flux.range(1, 10)
            .log()
            .onBackpressureBuffer(
                Duration.ofSeconds(3), // todo ttl время жизни события
                3, // todo храним максимум
                integer -> System.out.println("Dropped - " + integer), // todo Обработка переполнения
                Schedulers.boundedElastic() // todo планировщик потоков
            )
            .delayElements(Duration.of(10, ChronoUnit.SECONDS))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
    }

    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> { while (!disposable.isDisposed()) {}}
        );
    }

}
