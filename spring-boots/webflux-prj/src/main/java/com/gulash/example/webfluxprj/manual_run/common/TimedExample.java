package com.gulash.example.webfluxprj.manual_run.common;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Timed;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class TimedExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        //timedFlux();
        timedMono();
    }

    private static void timedFlux() {
        Flux<String> source = Flux.just("A", "B", "C", "D")
            .delayElements(Duration.ofMillis(500))
            .subscribeOn(Schedulers.boundedElastic());

        Flux<Timed<String>> timed = source
            // todo timed - получаем обёртку на событием Flux<Timed<X>>
            .timed()
            .doOnNext(
                // todo Timed<String> обёртка теперь элементом
                (Timed<String> timedItem) -> {
                    System.out.println("Элемент потока %s; Timestamp: %s, Elapsed from begin: %s, Elapsed previous step: %s".formatted(
                            timedItem.get(), // todo Значение
                            timedItem.timestamp(), // todo Метка времени
                            timedItem.elapsedSinceSubscription(), // todo Прошедшее время c подписки
                            timedItem.elapsed() // todo Прошедшее время с последнего шага
                        )
                    );
                }
            );

        Disposable disposable = timed.subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            Элемент потока A; Timestamp: 2025-01-16T18:45:21.693Z, Elapsed from begin: PT0.505089667S, Elapsed previous step: PT0.505089667S
            Элемент потока B; Timestamp: 2025-01-16T18:45:22.200Z, Elapsed from begin: PT1.012010084S, Elapsed previous step: PT0.506920417S
            Элемент потока C; Timestamp: 2025-01-16T18:45:22.702Z, Elapsed from begin: PT1.514430125S, Elapsed previous step: PT0.502420041S
            Элемент потока D; Timestamp: 2025-01-16T18:45:23.207Z, Elapsed from begin: PT2.019733625S, Elapsed previous step: PT0.5053035S
        */

    }
    private static void timedMono() {
        Mono<String> source = Mono.just("A")
            .delayElement(Duration.ofMillis(500))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<Timed<String>> timed = source
            // todo timed - получаем обёртку на событием Mono<Timed<X>>
            .timed()
            .doOnNext(
                // todo Timed<String> обёртка теперь элементом
                (Timed<String> timedItem) -> {
                    System.out.println("Элемент потока %s; Timestamp: %s, Elapsed from begin: %s, Elapsed previous step: %s".formatted(
                            timedItem.get(), // todo Значение
                            timedItem.timestamp(), // todo Метка времени
                            timedItem.elapsedSinceSubscription(), // todo Прошедшее время c подписки
                            timedItem.elapsed() // todo Прошедшее время с последнего шага
                        )
                    );
                }
            );

        Disposable disposable = timed.subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            Элемент потока A; Timestamp: 2025-02-01T14:40:08.344Z, Elapsed from begin: PT0.50756875S, Elapsed previous step: PT0.50756875S
        */
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
