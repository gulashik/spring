package com.gulash.example.webfluxprj.manual_run.flux.init;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class FromXxxxExample {
    public static void main(String[] args) {
        //fluxFrom();
        //fluxFromIterable();
        //fluxFromStream();
        fluxFromCallable();
        //fluxFromArray();

    }

    private static void fluxFrom() {
        Flux<Integer> fluxPublisher = Flux.just(1,2,3,4);

        // todo Flux.from() используется для создания Flux из другого Publisher
        Flux.from(fluxPublisher)
            .doFirst(() -> System.out.println("fromIterable start"))
            .doFinally(signalType -> System.out.println("fromIterable complete"))
            .doOnNext(integer -> System.out.println("first doOnNext: " + integer))
            .map(num -> num * num)
            .filter(num -> num % 2 == 0)
            .doOnNext(integer -> System.out.println("second doOnNext: " + integer))
            .subscribe();
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
        // todo - позволяет создавать реактивные потоки на основе стандартного Java-потока (Stream).
        //  todo Supplier<Stream> можно вызвать несколько раз
        Flux<Integer> fromStreamReusable = Flux.fromStream(() -> Stream.of(1, 2, 3, 4));
        //  todo Stream - чистый stream только один раз можно вызвать
        Flux<Integer> fromStreamOneOff = Flux.fromStream(Stream.of(1, 2, 3, 4));

        fromStreamOneOff
            .doFirst(() -> System.out.println("fromIterable start"))
            .doFinally(signalType -> System.out.println("fromIterable complete"))
            .doOnNext(integer -> System.out.println("first doOnNext: " + integer))
            .map(num -> num * num)
            .filter(num -> num % 2 == 0)
            .doOnNext(integer -> System.out.println("second doOnNext: " + integer))
            .subscribe();
        // будет IllegalStateException - stream has already been operated upon or closed
        // fromStreamOneOff.subscribe(sout -> System.out.println("2 call fromStreamOneOff: " + sout));

        fromStreamReusable.subscribe(sout -> System.out.println("1 call fromStreamReusable: " + sout));
        fromStreamReusable.subscribe(sout -> System.out.println("2 call fromStreamReusable: " + sout));

    }

    private static void fluxFromCallable() {
        // todo Flux.fromCallable НЕ СУЩЕСТВУЕТ НАПРЯМУЮ.
        //  Можно использовать Mono.fromCallable с конвертацией во Flux
        Mono<List<Integer>> mono = Mono.fromCallable(
            () -> {
                // Синхронная операция
                return List.of(1, 2, 3, 4);
            }
        );

        mono.flux() // Mono into Flux<List<Integer>>
            .flatMapIterable(lst -> lst) // into Flux<Integers>
            .subscribe(
                result -> System.out.println("Received: " + result),
                error -> System.err.println("Error: " + error)
            );
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