package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class SkipXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        skip();
        skipLast();
        skipUntil();
        skipWhile();
        skipUntilOther();
    }

    private static void skip() {
        Flux<Integer> numbers = Flux.range(1, 5)
            //.log()
            ;

        // todo skip - пропускает первые X элементов потока
        Flux<Integer> skipped = numbers.skip(2)
            .doFirst(() -> System.out.println("with skipped number"));

        Disposable disposableWithoutSkippedNumber = numbers.doFirst(() -> System.out.println("without skipped number"))
            .subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableWithoutSkippedNumber));

        Disposable disposableSkipped = skipped.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableSkipped));
        /*
            without skipped number
            1
            2
            3
            4
            5
            with skipped number
            3
            4
            5
        */
    }

    private static void skipLast() {
        Flux<Integer> numbers = Flux.range(1, 5)
            //.log()
            ;

        // todo skipLast - пропускает последние n элементов из последовательности.
        Flux<Integer> skipped = numbers.skipLast(2)
            .doFirst(() -> System.out.println("with skipped number"));

        Disposable withoutSkippedNumber = numbers.doFirst(() -> System.out.println("without skipped number"))
            .subscribe(System.out::println);

        Disposable disposableSkipped = skipped.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposableSkipped, withoutSkippedNumber));
        /*
            without skipped number
            1
            2
            3
            4
            5
            with skipped number
            1
            2
            3
        */
    }
    private static void skipUntil() {
        Flux<String> words = Flux.just("one", "two", "three", "start", "five");

        // todo skipUntil - пропускает элементы до тех пор, пока не выполнится определенное условие.
        Flux<String> skipUntilFlux = words.skipUntil(word -> word.startsWith("start"))
            .doFirst(() -> System.out.println("with skipped words"));

        Disposable disposableAll = words.doFirst(() -> System.out.println("without skipped words"))
            .subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableAll));

        Disposable disposableSkipped = skipUntilFlux.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableSkipped));
        /*
            without skipped words
            one
            two
            three
            start
            five
            with skipped words
            start
            five
        */
    }

    private static void skipWhile() {
        Flux<Integer> words = Flux.just(1, 2, 3, 4, 5);

        // todo skipWhile - пропускает элементы, пока выполняется заданное условие.
        Flux<Integer> skipUntilFlux = words.skipWhile(digit -> digit < 4)
            .doFirst(() -> System.out.println("with skipped digits"));

        Disposable disposableAll = words.doFirst(() -> System.out.println("without skipped digits"))
            .subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableAll));

        Disposable disposableSkipped = skipUntilFlux.subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposableSkipped));
        /*
            without skipped digits
            1
            2
            3
            4
            5
            with skipped digits
            4
            5
        */
    }

    private static void skipUntilOther() {
        Flux<String> source = Flux.just("A", "B", "C", "D", "E").delayElements(Duration.ofSeconds(1));
        Flux<Long> signal = Flux.interval(Duration.ofSeconds(3)).take(1); // todo Сигнал через 3 сек

        // todo skipUntilOther - Пропустим элементы до первого элемента другого потока
        Disposable disposable = source.skipUntilOther(signal)
            .subscribe(System.out::println);
        // Вывод (через 3 сек): C, D, E

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
