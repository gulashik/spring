package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.List;

public class IndexExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        // todo возвращает Flux<Tuple2<Long, ЭлементПотока>>
        Flux<Tuple2<Long, Integer>> index = Flux.range(10, 3)
            // todo index - присваивает каждому элементу из потока индекс (или ключ), начиная с 0.
            .index();

        Disposable disposable = index
            .doOnNext(
                // todo обрабатываем Flux<Tuple2<Long, ЭлементПотока>>
                (Tuple2<Long, Integer> tuple2) -> {
                    System.out.println("tuple2: %s; index: %s; value: %s".formatted(tuple2, tuple2.getT1(), tuple2.getT2()));
                    //tuple2.iterator().forEachRemaining(System.out::println);
                }
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            tuple2: [0,10]; index: 0; value: 10
            [0,10]
            tuple2: [1,11]; index: 1; value: 11
            [1,11]
            tuple2: [2,12]; index: 2; value: 12
            [2,12]
        */

        Flux<Long> indexed = Flux.range(10, 3)
            // todo index(index, value) - получаем индекс и значение на вход(как map + индекс элемента)
            .index(
                (aLong, integer) -> {
                    System.out.println("index: " + aLong + "; value: " + integer);
                    return integer * aLong; // вычисляем новое значение на основе
                }
            );

        Disposable disposable1 = indexed
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable1));
        /*
            index: 0; value: 10
            0
            index: 1; value: 11
            11
            index: 2; value: 12
            24
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
