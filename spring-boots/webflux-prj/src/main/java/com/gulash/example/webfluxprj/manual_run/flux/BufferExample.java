package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class BufferExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        bufferExample();
        bufferTimeoutExample();
        bufferUntilExample();
        bufferUntilChangedExample();
        bufferWhileExample();
        bufferWhenExample();
    }

    private static void bufferExample() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // todo buffer - собирает элементы потока в группы, создавая новый Flux<List<>>
        Flux<List<Integer>> bufferedFlux = flux.buffer(3);

        bufferedFlux.subscribe(list -> System.out.println("Буфер: " + list));
        /*
            Буфер: [1, 2, 3]
            Буфер: [4, 5, 6]
            Буфер: [7, 8, 9]
            Буфер: [10]
        */

    }

    private static void bufferTimeoutExample() {
        Flux<List<Long>> listFlux = Flux.interval(Duration.ofMillis(100)) // каждую 100 мс генерируется элемент
            .take(10) // берем первые 10 элементов
            // todo bufferTimeout -  Буферизует элементы до определенного времени
            //  или пока не будет набрано заданное количество элементов, в зависимости от того, что наступит раньше.
            .bufferTimeout(3, Duration.ofMillis(400));// todo буфер на 3 элемента или 400 мс

        Disposable disposable = listFlux.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            [0, 1, 2]
            [3, 4, 5]
            [6, 7, 8]
            [9]
        */
    }

    private static void bufferUntilExample() {
        Flux<List<Long>> listFlux = Flux.interval(Duration.ofMillis(100)) // каждую 100 мс генерируется элемент
            .take(10) // берем первые 10 элементов
            // todo bufferTimeout -  Буферизует элементы до тех пор, пока выполняется условие.
            //  При первом элементе, не соответствующем условию, буфер сбрасывается.
            .bufferUntil(i -> i % 3 == 0); // todo буферизуем до тех пор, пока число не делится на 3

        Disposable disposable = listFlux.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            [0]
            [1, 2, 3]
            [4, 5, 6]
            [7, 8, 9]
        */
    }

    private static void bufferUntilChangedExample() {
        Flux<Integer> flux = Flux.just(1, 1, 2, 2, 3, 3, 3, 4, 5, 5);

        Flux<List<Integer>> listFlux1 = flux
            // todo bufferUntilChanged -  Буферизует элементы до тех пор, пока они не изменятся (сравниваются с предыдущим элементом)
            .bufferUntilChanged();
        /*
            bufferUntilChanged() [1, 1]
            bufferUntilChanged() [2, 2]
            bufferUntilChanged() [3, 3, 3]
            bufferUntilChanged() [4]
            bufferUntilChanged() [5, 5]
        */
        Flux<List<Integer>> listFlux2 = flux
            // todo bufferUntilChanged - буферизуем до тех смены число делится/не делится на 3
            .bufferUntilChanged(i -> {
                    boolean res = i % 3 == 0;
                    //System.out.printf("current-%s; i%%3-%s; res-%s%n ", i, i%3, res);
                    return res;
                }
            );
        /*
            bufferUntilChanged(keySelector) [1, 1, 2, 2]
            bufferUntilChanged(keySelector) [3, 3, 3]
            bufferUntilChanged(keySelector) [4, 5, 5]
        */
        Disposable disposable1 = listFlux1.subscribe(longs -> System.out.println("bufferUntilChanged() " + longs));
        waitForDisposableEnd(List.of(disposable1));

        Disposable disposable2 = listFlux2.subscribe(longs -> System.out.println("bufferUntilChanged(keySelector) " + longs));
        waitForDisposableEnd(List.of(disposable2));

    }

    private static void bufferWhileExample() {
        Disposable disposable = Flux.just(1, 2, 3, 4, 5, 1, 2, 3)
            // todo bufferWhile - Буферизует элементы, пока выполняется условие.
            //      Как только условие перестает выполняться, буфер сбрасывается.
            .bufferWhile(i -> i < 4) // буферизуем, пока элемент меньше 4
            .subscribe(System.out::println);

        /*
            [1, 2, 3]
            [1, 2, 3]
        */
        waitForDisposableEnd(List.of(disposable));
    }

    private static void bufferWhenExample() {
        Flux<Long> source = Flux.interval(Duration.ofMillis(100)).take(10);

        // todo bufferWhen - Буферизует элементы в периодах, определяемых начальным (start) и закрывающим (end) сигналами.
        Disposable disposable = source.bufferWhen(
                Flux.interval(Duration.ofMillis(300)),  // каждые 300 мс стартует новый буфер
                start -> Mono.delay(Duration.ofMillis(200)) // буфер длится 200 мс
            )
            .subscribe(System.out::println);

        /*
            [3, 4]
            [5, 6, 7]
            [8, 9]
        */
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
