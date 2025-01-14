package com.gulash.example.webfluxprj.manual_run.flux.block;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Stream;

public class ToXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        toIterable();
        toStream();
    }
    /*
        НЕ ИСПОЛЬЗУЕМ subscribe
        БЛОКИРУЮЩИЕ МЕТОДЫ

        Применение:
            Применяется в тестах.
            Используется в местах, где требуется интеграция реактивного кода с императивным.
    */

    private static void toStream() {
        // todo toStream — метод, который преобразует реактивный источник данных в Stream
        //  значения по умолчанию для backpressure (обычно 256 элементов)
        Stream<Integer> stream = Flux.range(1, 5).delayElements(Duration.ofSeconds(1))
            .doFirst(() -> System.out.println("toStream()"))
            .publishOn(Schedulers.boundedElastic())
            .toStream();// todo не вызываем subscribe и загружает 256 элементов за раз

        stream.forEach(System.out::println);

        // todo toStream — метод, который преобразует реактивный источник данных в Stream
        Stream<Integer> stream1 = Flux.range(1, 5).delayElements(Duration.ofSeconds(1))
            .doFirst(() -> System.out.println("toStream(batchSize)"))
            .toStream(3);// todo не вызываем subscribe и загружает batchSize элементов за раз

        stream1.forEach(System.out::println);

    }

    private static void toIterable() {
        // todo toIterable — метод, который превращает поток данных в объект типа Iterable.
        //  значения по умолчанию для backpressure (обычно 256 элементов)
        Iterable<Integer> iterable = Flux.range(1, 5).delayElements(Duration.ofSeconds(1))
            .doFirst(() -> System.out.println("toIterable()"))
            .publishOn(Schedulers.boundedElastic())
            .toIterable(); // todo не вызываем subscribe и загружает 256 элементов за раз

        iterable.forEach(System.out::println);

        // todo toIterable — метод, который превращает поток данных в объект типа Iterable.
        Iterable<Integer> iterable1 = Flux.range(1, 5).delayElements(Duration.ofSeconds(1))
            .doFirst(() -> System.out.println("toIterable(batchSize)"))
            .toIterable(3); // todo не вызываем subscribe и загружает batchSize элементов за раз

        iterable1.forEach(System.out::println);

        // todo toIterable — метод, который превращает поток данных в объект типа Iterable.
        Iterable<Integer> iterable2 = Flux.range(1, 5).delayElements(Duration.ofSeconds(1))
            .doFirst(() -> System.out.println("toIterable(T batchSize, Supplier<Queue<T>)"))
            .publishOn(Schedulers.boundedElastic())
            .toIterable(
                // todo сколько элементов загружаем за раз
                3,
                // todo какую очередь использовать для временного хранения элементов.
                () -> new PriorityQueue<Integer>(10)// todo defauly ArrayBlockingQueue; можно указать разные LinkedBlockingQueue, PriorityQueue
            );// todo не вызываем subscribe

        iterable2.forEach(System.out::println);
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
