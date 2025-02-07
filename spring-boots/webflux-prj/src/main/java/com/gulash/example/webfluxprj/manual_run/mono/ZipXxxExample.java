package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.List;

public class ZipXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        zip();
        zipDelayError();
        zipWhen();
        zipWith();
    }

    private static void zip() {
        // todo возвращает Mono возвращаемого типа
        Mono<String> zippedCombinator =
            // todo zip - ассинхронно выполняет все Publisher и потом Выполняется функция(на вход все значения)
            Mono.zip(
                    // todo Несколько Mono для zip операции
                    Mono.just(1).doFirst(() -> System.out.println("with combinator 1")),
                    Mono.just("Two").doFirst(() -> System.out.println("with combinator Two")),
                    // todo combinator -  принимает несколько входных значений и комбинирует их в одно значение.
                    (Integer i1, String s2) -> i1 + "-" + s2
                )
                .doFirst(() -> System.out.println("-----zip with combinator-----"))
                .doOnSuccess(s -> System.out.println("success: " + s));

        // todo возвращает Tuple в Mono возвращаемого типа
        Mono<Tuple2<Integer, String>> zippedTuple = Mono.zip(
                // todo Несколько Mono для zip операции
                Mono.just(1).doFirst(() -> System.out.println("with tuple 1")),
                Mono.just("Two").doFirst(() -> System.out.println("with tuple Two"))
            )
            .doFirst(() -> System.out.println("-----zip with tuple-----"))
            .doOnSuccess(s -> System.out.println("success: " + s));

        Disposable disposable1 = zippedCombinator
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);
        waitForDisposableEnd(List.of(disposable1));

        Disposable disposable2 = zippedTuple
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                (Tuple2<Integer, String> tuple2) -> System.out.println(tuple2.getT1() + "-" + tuple2.getT2())
            );
        waitForDisposableEnd(List.of(disposable2));
    }

    private static void zipWith() {
        Mono<Integer> monoFirst = Mono.just(1).doOnNext((Integer i) -> System.out.println("monoFirst: " + i));
        Mono<Integer> monoSecond = Mono.just(2).doOnNext((Integer i) -> System.out.println("monoSecond: " + i));

        // todo Mono<ИтоговыйТип>
        Mono<String> monoResult =
            // todo zipWith - аналогично zip
            monoFirst.zipWith(
            // todo Publisher для выполнения
            monoSecond,
            // todo combinator -  принимает входные значения от Publisher-ов и комбинирует их в одно значение.
            (Integer i1, Integer i2) -> {
                String s = i1 + "<->" + i2;
                System.out.println("combinator calc value: " + s);
                return s;
            }
        );

        Disposable disposable = monoResult
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(s -> { System.out.println("subscribe run: " + s); });
        waitForDisposableEnd(List.of(disposable));

        /*
            monoFirst: 1
            monoSecond: 2
            combinator calc value: 1<->2
            subscribe run: 1<->2
        */
    }

    private static void zipWhen() {

        Mono<Integer> monoFirst = Mono.just(1)
            .doOnNext(s -> System.out.println("Publisher1 doOnNext: " + s));

        Mono<String> zippedWhen =
            // todo zipWhen - 1 подаёт на вход другому Publisher-у результат + 2 функция обработки двух результатов
            monoFirst.zipWhen(
                // todo Publisher2 на вход результат Publisher1
                (Integer monoFirstValue) -> Mono.just(2 + monoFirstValue).doOnNext(s -> System.out.println("Publisher2 doOnNext: " + s)),
                // todo combinator - функция обработка результатов двух Publisher-ов
                (Integer i1, Integer i2) -> "Combinator Publisher1 value:'%s'; Publisher2 value:'%s'".formatted(i1, i2)
            );

        Disposable disposable = zippedWhen
            .doOnNext(s -> System.out.println("doOnNext: " + s))
            .subscribe();
        waitForDisposableEnd(List.of(disposable));
        /*
            Publisher1 doOnNext: 1
            Publisher2 doOnNext: 3
            doOnNext: Combinator Publisher1 value:'1'; Publisher2 value:'3'
        */
    }

    private static void zipDelayError() {

        // todo возвращает Mono возвращаемого типа
        Mono<String> zippedCombinator =
            // todo zipDelayError - ассинхронно выполняет все Publisher и потом Выполняется функция(на вход все значения)
            //      при ошибке ожидание завершения остальных Publisher
            Mono.zipDelayError(
                    // todo combinator -  принимает несколько входных значений и комбинирует их в одно значение.
                    (Object[] obj) -> obj[0] + "-" + obj[1] + "-" + obj[2],
                    // todo Несколько Mono для zip операции
                    Mono.<String>just("1").delayElement(Duration.ofMillis(100)).doOnNext(s -> System.out.println("doOnNext: " + s)),
                    //Mono.<String>just("1").doFirst(() -> System.out.println("with combinator 1")),
                    Mono.<String>error(new RuntimeException("Exception")).doOnNext(s -> System.out.println("doOnNext: " + s)),
                    Mono.<String>just("Two").delayElement(Duration.ofMillis(50)).doOnNext(s -> System.out.println("doOnNext: " + s))
                )
                .doFirst(() -> System.out.println("-----zipDelayError with combinator-----"))
                .doOnSuccess(s -> System.out.println("success: " + s));

        Disposable disposable1 = zippedCombinator
            .subscribeOn(Schedulers.parallel())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable1));

        /*  Exception случился дождались завершения всех Mono
            -----zipDelayError with combinator-----
            doOnNext: Two
            doOnNext: 1
        */

        // todo возвращает Tuple в Mono возвращаемого типа
        Mono<Tuple3<Integer, Integer, String>> zippedTuple =
            // todo zipDelayError - ассинхронно выполняет все Publisher и потом Выполняется функция(на вход все значения)
            //      при ошибке ожидание завершения остальных Publisher
            Mono.zipDelayError(
                    // todo Несколько Mono для zip операции
                    Mono.just(1).delayElement(Duration.ofMillis(100)).doOnNext(s -> System.out.println("doOnNext: " + s)),
                    Mono.just(2).doOnNext(s -> System.out.println("doOnNext: " + s)),
                    Mono.just("Two").delayElement(Duration.ofMillis(50)).doOnNext(s -> System.out.println("doOnNext: " + s))
                )
                .doFirst(() -> System.out.println("-----zipDelayError with tuple-----"))
                .doOnSuccess(s -> System.out.println("success: " + s));

        Disposable disposable2 = zippedTuple
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                (Tuple3<Integer, Integer, String> tuple2) -> System.out.println(tuple2.getT1() + "-" + tuple2.getT2() + "-" + tuple2.getT3())
            );
        waitForDisposableEnd(List.of(disposable2));
    /*
        -----zipDelayError with tuple-----
        doOnNext: 2
        doOnNext: Two
        doOnNext: 1
        success: [1,2,Two]
    */
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> {
                while (!disposable.isDisposed()) {
                }
            }
        );
    }
}
