package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

public class SingleXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> mono = Mono.just("data value");

        Mono<String> single = mono
            // todo single - ожидает одно и только одно значение
            //  Если Mono пустой, будет выброшено NoSuchElementException.
            //  Если Mono содержит более одного элемента, будет выброшено IndexOutOfBoundsException.
            .single();

        Disposable disposable = single
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(s -> System.out.println("single: " + s));

        waitForDisposableEnd(List.of(disposable));

        Mono<Optional<String>> optionalMono = mono
            // todo singleOptional - значение из Mono оборачивает в Optional
            .singleOptional();

        optionalMono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(s -> System.out.println("optionalMono: " + s));

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
