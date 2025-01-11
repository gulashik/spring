package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class BufferExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // todo buffer - собирает элементы потока в группы, создавая новый Flux<List<>>
        Flux<List<Integer>> bufferedFlux = flux.buffer(3);

        bufferedFlux.subscribe(list -> System.out.println("Буфер: " + list));
        /*
            Буфер: [1, 2, 3]
            Буфер: [4, 5, 6]
            Буфер: [7, 8, 9]
            Буфер: [10]
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
