package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class CollectExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<String> words = Flux.just("hello", "world", "hello", "hello", "hello", "hello");

        // todo collect - позволяет собирать элементы потока в произвольную структуру данных, предоставляя коллектор.
        //      Коллектор – это интерфейс, описывающий операции, необходимые для создания и наполнения коллекции.
        // todo Set
        Mono<Set<String>> uniqueWords = words.collect(Collectors.toSet()); // [world, hello]
        // todo List
        Mono<List<String>> listMono = words.collect(Collectors.toList()); // [hello, world, hello, hello, hello, hello]
        // todo Map
        Mono<Map<String, String>> collectedWithMerge = words
            .collect(
                Collectors.toMap(
                    s -> s, /*key*/
                    s -> s, /*value*/
                    (cum, cur_s) -> new StringJoiner(", ").add(cum).add(cur_s).toString(), /*merge function если дубликаты*/
                    TreeMap::new /*можно указать конкретный тип*/
                )
            ); // {hello=hello, hello, hello, hello, hello, world=world}

        uniqueWords.subscribe(System.out::println);
        listMono.subscribe(System.out::println);
        collectedWithMerge.subscribe(System.out::println);
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
