package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class ElementAtExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {

        Flux<Integer> flux = Flux.just(1, 2, 3, 4, 5).publishOn(Schedulers.boundedElastic());
        // todo elementAt - получение элемента по индексу получаем Mono<X>
        //  индекс превышает IndexOutOfBoundsException
        Mono<Integer> element = flux.elementAt(0/*todo индекс с нуля*/);
        Mono<Integer> elementException = flux.elementAt(10/*todo индекс с нуля*/);
        Mono<Integer> elementWithDefault = flux.elementAt(10/*todo индекс с нуля*/, 99/*todo default value*/);

        Disposable disposable1 = element.subscribe(System.out::println);
        // 1

        Disposable disposable2 = elementException.subscribe(
            System.out::println,
            Throwable::printStackTrace, // сработает
            () -> System.out.println("Done")
            );
        // java.lang.IndexOutOfBoundsException: source had 5 elements, expected at least 11

        Disposable disposable3 = elementWithDefault.subscribe(System.out::println);
        // 99

        waitForDisposableEnd(
            List.of(disposable1, disposable2, disposable3)
        );
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
