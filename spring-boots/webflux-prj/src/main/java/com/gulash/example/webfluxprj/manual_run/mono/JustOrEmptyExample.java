package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class JustOrEmptyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {

        Mono<String> mono =
            // todo justOrEmpty - помогает избежать NullPointerException
            Mono.<String>justOrEmpty(null)
            //Mono.just(null) // будет NullPointerException
            ;

        Disposable disposable = mono
            // можно дополнительно использовать с ...IfEmpty методами
            .defaultIfEmpty("default for empty")
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
