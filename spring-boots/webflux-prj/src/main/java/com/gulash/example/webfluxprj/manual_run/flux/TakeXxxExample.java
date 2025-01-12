package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class TakeXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        take();
        takeLast();
        takeUntil();
        takeWhile();
        takeUntilOther();
    }

    private static void take() {
        Flux<Integer> flux = Flux.range(1, 10)
            .subscribeOn(Schedulers.boundedElastic());

        // todo take - Ограничивает поток до заданного количества элементов.
        Disposable disposable = flux.take(3)
            .doFirst(() -> System.out.println("Только первые 3"))
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Только первые 3
            1
            2
            3
        */
    }

    private static void takeLast() {
        Flux<Integer> flux = Flux.range(1, 10)
            .subscribeOn(Schedulers.boundedElastic());

        // todo takeLast - эмитит ПОСЛЕДНИЕ N элементов из потока.
        Disposable disposable = flux.takeLast(3)
            .doFirst(() -> System.out.println("Только последние 3"))
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Только последние 3
            8
            9
            10
        */
    }

    private static void takeUntil() {
        Flux<Integer> flux = Flux.range(1, 10)
            .subscribeOn(Schedulers.boundedElastic());

        // todo takeUntil - эмитит элементы, пока УСЛОВИЕ НЕ ВЫПОЛНИТСЯ.
        //      КАК ТОЛЬКО УСЛОВИЕ СТАНОВИТСЯ ИСТИННЫМ, ПОТОК ЗАВЕРШАЕТ РАБОТУ - т.е. будет один элемент превышающий условие
        Disposable disposable = flux.takeUntil(i -> i > 3)
            .doFirst(() -> System.out.println("ДО первого true; условие не больше 3-х и один элемент превышающий условие"))
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            ДО первого true; условие не больше 3-х и один элемент превышающий условие
            1
            2
            3
            4
        */
    }

    private static void takeWhile() {
        Flux<Integer> flux = Flux.range(1, 10)
            .subscribeOn(Schedulers.boundedElastic());

        // todo takeUntil - эмитит элементы, ПОКА УСЛОВИЕ ИСТИННО.
        //      Как только условие становится ЛОЖНЫМ, ПОТОК ЗАВЕРШАЕТСЯ.
        Disposable disposable = flux.takeWhile(i -> i < 3)
            .doFirst(() -> System.out.println("ПОКА true; условие не больше 3-х"))
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            ПОКА true; условие не больше 3-х
            1
            2
        */
    }

    private static void takeUntilOther() {
        Flux<Integer> flux = Flux.range(1, 10)
            .delayElements(Duration.ofMillis(100))
            .subscribeOn(Schedulers.boundedElastic());

        Mono<String> signal = Mono.just("go").delayElement(Duration.ofMillis(400));

        // todo takeUntilOther - эмитит элементы до тех пор, пока другой поток не начнет эмитить элементы или не завершится.
        Disposable disposable = flux.takeUntilOther(signal) // todo Остановится, как только trigger излучит значение
            .doFirst(() -> System.out.println("Работает пока не получим первое сообщение из другого Publisher"))
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Работает пока не получим первое сообщение из другого Publisher
            1
            2
            3
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
