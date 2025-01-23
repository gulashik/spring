package com.gulash.example.webfluxprj.manual_run;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Timed;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.List;

public class ElapsedExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<String> source = Flux.just("A", "B", "C", "D")
            .delayElements(Duration.ofMillis(500))
            .subscribeOn(Schedulers.boundedElastic());

        Flux<Tuple2<Long, String>> elapsed = source
            // todo elapsed - Измеряем время в  милии секундах между элементами получаем обёртку над событием Flux<Tuple2<Long, X>>
            //  todo elapsed не модифицирует данные потока, а добавляет временную метку к каждому элементу.
            .elapsed(/*Опционально Schedulers.parallel()*/) // Измеряем время между элементами
            //.log()
            .doOnNext(
                // todo Tuple2<Long, X> обёртка теперь элементом
                (Tuple2<Long, String> tuple2) -> {
                    System.out.println("Elapsed previous step: %s, Элемент потока %s".formatted(
                            tuple2.getT1(), // todo Значение
                            tuple2.getT2() // todo Метка времени
                        )
                    );
                }
            );

        Disposable disposable = elapsed.subscribe();

        waitForDisposableEnd(List.of(disposable));
        /*
            Elapsed previous step: 506, Элемент потока A
            Elapsed previous step: 507, Элемент потока B
            Elapsed previous step: 502, Элемент потока C
            Elapsed previous step: 501, Элемент потока D
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
