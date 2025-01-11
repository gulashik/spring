package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReduceExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Integer> numbers = Flux.range(1, 5);
        // todo reduce - позволяет сводить поток элементов к единственному значению, применяя к ним заданную функцию аккумуляции.
        // todo резальтат получаем в Mono<>
        Mono<Integer> sum = numbers.reduce(0, (cum, current_integer) -> Integer.sum(cum, current_integer));

        sum.subscribe(System.out::println);
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
