package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class CastExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Object> flux = Flux.just(1, "two", 3.0);

        // todo был Flux<Object> стал Flux<Number>
        Flux<Number> cast = flux
            // фильтруем иначе ошибка будет - java.lang.ClassCastException: Cannot cast java.lang.String to java.lang.Number
            .filter(s -> s instanceof Number)
            // todo cast - используется для приведения объекта (обычно в рамках реактивного потока) к конкретному типу.
            .cast(Number.class);

        Disposable disposable = cast
            .doOnError(throwable -> throwable.printStackTrace())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

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
