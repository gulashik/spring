package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class AllAnyExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        all();
        any();
    }

    private static void all() {
        Flux<Integer> flux = Flux.just(2, 4, 6);

        // todo all - все элементы удовлетворяют предикату получаем Mono<Boolean>
        //  Завершается, как только найден элемент, который не соответствует предикату (оптимизация).
        //  Если поток пустой, возвращает true.
        Mono<Boolean> allEven = flux.all(num -> num % 2 == 0);

        allEven.subscribe(result -> System.out.println("Are all even? " + result));
    }
    private static void any() {
        Flux<Integer> flux = Flux.just(1, 3, 5, 7);

        // todo any - хотя бы один элемент удовлетворяет предикату получаем Mono<Boolean>
        //  Завершается, как только найден элемент, который соответствует предикату (оптимизация).
        //  Если поток пустой, возвращает false.
        Mono<Boolean> hasEven = flux.any(num -> num % 2 == 0);

        hasEven.subscribe(result -> System.out.println("Is there any even number? " + result));
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
