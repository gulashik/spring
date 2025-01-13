package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class WithLatestFromExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        withLatestFrom();
    }

    private static void withLatestFrom() {
        Flux<Integer> mainFlux = Flux.range(1, 5).delayElements(Duration.ofSeconds(3), Schedulers.boundedElastic());

        Flux<Integer> otherFlux = Flux.range(1,10).delayElements(Duration.ofSeconds(1), Schedulers.boundedElastic());

        // todo withLatestFrom - событию из основного потока сопостовляется последнее полученное из другого
        Disposable disposable = mainFlux
            .doFirst(() -> System.out.println("Событию mainFlux получаем последнее полученное из otherFlux"))
            .withLatestFrom(
                otherFlux, // другой поток; будет браться последнее событие на момент получения из основного
                (mainItem, otherItem) -> "mainItem: " + mainItem + " otherItem: " + otherItem
            )
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Событию mainFlux получаем последнее полученное из otherFlux
            mainItem: 1 otherItem: 2
            mainItem: 2 otherItem: 5
            mainItem: 3 otherItem: 8
            mainItem: 4 otherItem: 10
            mainItem: 5 otherItem: 10
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
