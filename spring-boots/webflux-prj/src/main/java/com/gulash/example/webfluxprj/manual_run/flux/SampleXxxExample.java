package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class SampleXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        sample();
        sampleTimeout();
    }

    private static void sample() {
        Flux<Integer> flux = Flux
            .range(10, 20)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(t -> System.out.println("emitted: " + t))
            ;
        // Зачем
        // отфильтровать элементы потока, которые поступают слишком быстро
        Disposable disposable = flux
            // todo sample - используется для того, чтобы выбирать ПОСЛЕДНИЙ элемент из потока, основываясь на периодичности.
            //.sample(Duration.ofMillis(500)) // Берет последний элемент за каждые 500 миллисекунд

            // todo sampleFirst - используется для того, чтобы выбирать ПЕРВЫЙ элемент из потока, основываясь на периодичности.
            .sampleFirst(Duration.ofMillis(500)) // Берет последний элемент за каждые 500 миллисекунд

            .subscribe(System.out::println);


        waitForDisposableEnd(List.of(disposable));

        Disposable disposable2 = flux
            // todo sample ПОСЛЕДНЕГО элемента, на основании ЭМИССИИ ДРУГОГО ПОТОКА
            //.sample( Flux.just("make sample 1", "make sample 2").delayElements(Duration.ofMillis(500)))

            // todo sample ПЕРВЫЙ элемент, на основании ЭМИССИИ ДРУГОГО ПОТОКА
            .sampleFirst( item -> Mono.delay(Duration.ofMillis(300)) )


            .subscribe(System.out::println);


        waitForDisposableEnd(List.of(disposable2));
    }

    private static void sampleTimeout() {
        Flux<Integer> flux = Flux
            .range(10, 20)
            // todo эмиттим через разные промежутки времени
            .delayElements(Duration.ofMillis(new Random().nextInt(90, 110)))
            .doOnNext(t -> System.out.println("emitted: " + t))
            ;
        // Зачем
        // отфильтровать элементы потока, которые поступают слишком быстро
        Disposable disposable = flux
            // todo sampleTimeout - выбирает ПОСЛЕДНИЙ элемент из потока при ПРЕВЫШЕНИИ ТАЙМАУТА
            // на входе задержка разная, но если она более 100 миллисекунд, то делаем sample
            .sampleTimeout( item -> Mono.delay(Duration.ofMillis(100)) )
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