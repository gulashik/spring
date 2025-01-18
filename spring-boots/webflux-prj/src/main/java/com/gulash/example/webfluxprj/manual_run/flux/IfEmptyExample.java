package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class IfEmptyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        ifEmpty();
    }

    private static void ifEmpty() {

        Flux<String> emptyStringFlux = Flux.empty();

        Flux<String> stringFlux1 = emptyStringFlux
            // todo defaultIfEmpty - указываем ЗНАЧЕНИЕ если поток пустой
            .defaultIfEmpty("default value from literal");

        stringFlux1.subscribe(System.out::println);

        Flux<String> stringFlux2 = emptyStringFlux
            // todo switchIfEmpty - указываем PUBLISHER если поток пустой
            .switchIfEmpty(Flux.just("default value from Publisher"));

        stringFlux2.subscribe(System.out::println);
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
