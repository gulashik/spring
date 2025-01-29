package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class HandleExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        example();
    }

    private static void example() {
        Mono<String> handled = Mono.just("some data")
            // todo handle - позволяет модифицировать или фильтровать элементы(несколько методов в одном)
            .handle(
                (String data, SynchronousSink<String> sink) -> {
                    if (data != null && !data.isEmpty()) {
                        String processedData = data.toUpperCase();
                        sink.next(processedData);
                    } else {
                        sink.error(new RuntimeException("No data received"));
                    }
                }
            );

        Disposable disposable = handled
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);
        // SOME DATA

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
