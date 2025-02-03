package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class IfEmptyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        ifEmpty();
    }

    private static void ifEmpty() {

        Mono<String> empty = Mono.empty();

        Mono<String> mono1 = empty
            // todo defaultIfEmpty - указываем ЗНАЧЕНИЕ если поток пустой
            .defaultIfEmpty("default value from literal");

        mono1.subscribe(System.out::println);

        Mono<String> mono2 = empty
            // todo switchIfEmpty - указываем PUBLISHER если поток пустой
            .switchIfEmpty(Mono.just("default value from Publisher"));

        mono2.subscribe(System.out::println);
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
