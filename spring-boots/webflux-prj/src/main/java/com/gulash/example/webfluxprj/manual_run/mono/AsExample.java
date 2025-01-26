package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class AsExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> monoString = Mono.just("One two");

        // todo as - преобразуем в другое Mono с добавлением других опций
        Mono<Integer> transformedMono = monoString.as(
            (Mono<String> stringMono) ->
                stringMono
                    .map(String::length)
                    .doFirst(() -> System.out.println("add some actions"))
        );

        Disposable disposable = transformedMono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);// 7

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
