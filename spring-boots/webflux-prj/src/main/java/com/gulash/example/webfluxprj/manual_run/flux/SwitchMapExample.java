package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class SwitchMapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4)
            .publishOn(Schedulers.boundedElastic())
            // todo ПОЛЕЗНОСТЬ если нужно игнорировать старые запросы и обработать только самый последний элемент.
            .switchMap(i -> {
                return Flux.just(i * 10)
                    // todo Симуляция задержки
                    .delayElements(Duration.ofMillis(1)) // todo закомментить, чтобы не было задержки
                    ;
            });

        Disposable disposable = flux.subscribe(result -> System.out.println("Result: " + result));
        waitForDisposableEnd(List.of(disposable));
        /*  ЕСЛИ ЕСТЬ ЗАДЕРЖКА хотя бы на миллисекунду, то работает
                Result: 40
            ЕСЛИ НЕТ ЗАДЕРЖКИ, то видим все элементы работает
                Result: 10
                Result: 20
                Result: 30
                Result: 40
        */
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
