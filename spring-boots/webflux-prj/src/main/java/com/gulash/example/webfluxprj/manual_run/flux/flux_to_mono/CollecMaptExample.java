package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

public class CollecMaptExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<String> words = Flux.just("hello", "world", "hello", "hello", "hello", "hello");

        // todo collectMap - позволяет собирать элементы потока в Map. НЕТ ОБРАБОТКИ КОЛЛИЗИИ ПО КЛЮЧАМ. Используем collect, collectMultimap
        Mono<Map<String, String>> mapMono = words
            .collectMap(
                s -> s, /*key*/
                s -> s, /*value*/
                LinkedHashMap::new /*можно указать конкретный тип*/
            );// {hello=hello, world=world}

        mapMono.subscribe(System.out::println);

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
