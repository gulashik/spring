package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class FlatMapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        flatMap();
    }

    private static void flatMap() {
        Mono<String> mono = Mono.just("Hello")
            // todo flatMap - для асинхронного преобразования элементов, возвращая новый Mono в результате маппинга
            .flatMap(value -> Mono.just(value + " World!"));
        mono.subscribe(System.out::println);  //  Hello World!
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
