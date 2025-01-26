package com.gulash.example.webfluxprj.manual_run.common;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;

public class TimestampExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {

        Flux<Tuple2<Long, String>> timestamped = Flux
            .just("A", "B", "C", "D")
            .delayElements(Duration.ofMillis(500))
            // todo timestamp - оборачивает элемент в Tuple2<Long, ElementType>, где Long - количество миллисекунд с 00:00:00 1 января 1970 года
            .timestamp(Schedulers.boundedElastic());

        Disposable disposable = timestamped.subscribe(
            (Tuple2<Long, String> tuple2) -> {
                System.out.println("Timestamp: " + tuple2.getT1() + ", Элемент потока: " + tuple2.getT2());
            }
        );

        waitForDisposableEnd(List.of(disposable));
        /*
            Timestamp: 1737486241176, Элемент потока: A
            Timestamp: 1737486241683, Элемент потока: B
            Timestamp: 1737486242188, Элемент потока: C
            Timestamp: 1737486242693, Элемент потока: D
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
