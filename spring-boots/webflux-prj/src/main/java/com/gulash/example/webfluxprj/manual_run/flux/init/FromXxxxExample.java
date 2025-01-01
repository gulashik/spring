package com.gulash.example.webfluxprj.manual_run.flux.init;

import reactor.core.publisher.Flux;

import java.util.List;

public class FromXxxxExample {
    public static void main(String[] args) {
        //fluxFromIterable();
        fluxFromArray();

    }

    private static void fluxFromIterable() {
        // todo fromIterable - из любой коллекции, реализующей интерфейс Iterable (например, List, Set и т.д.).
        Flux<Integer> fromIterable = Flux.fromIterable(List.of(1, 2, 3, 4));

        fromIterable
            .doFirst(() -> System.out.println("fromIterable start"))
            .doFinally(signalType -> System.out.println("fromIterable complete"))
            .doOnNext(integer -> System.out.println("first doOnNext: " + integer))
            .map(num -> num * num)
            .filter(num -> num % 2 == 0)
            .doOnNext(integer -> System.out.println("second doOnNext: " + integer))
            .subscribe();
    }

    private static void fluxFromStream() {

    }

    private static void fluxFromArray() {
        Integer[] names = {1, 2, 3, 4};
        // todo fromArray - для создания реактивных стримов из массивов.
        Flux<Integer> stringFlux = Flux.fromArray(names);

        stringFlux
            .doFirst(() -> System.out.println("fromIterable start"))
            .doFinally(signalType -> System.out.println("fromIterable complete"))
            .doOnNext(integer -> System.out.println("first doOnNext: " + integer))
            .map(num -> num * num)
            .filter(num -> num % 2 == 0)
            .doOnNext(integer -> System.out.println("second doOnNext: " + integer))
            .subscribe();
    }
}