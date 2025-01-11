package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class AtIndexExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        exampleNextLast();
    }
    private static void exampleNextLast() {
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4)
            .delayElements(Duration.ofMillis(new Random().nextInt(100, 300)));

        // Неблокирующий вариант с использованием first(), next()
        // todo next - следующий(первый) элемент в Mono<X>
        Mono<Integer> firstNumberMono = numbers.next();
        // todo last - последний элемент в Mono<X>
        Mono<Integer> lastNumberMono = numbers.last();
        Mono<Integer> lastNumberMono2 = numbers.last(99/*default value*/);

        Disposable disposable1 = firstNumberMono.subscribe(System.out::println);
        Disposable disposable2 = lastNumberMono.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable1, disposable2));
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
