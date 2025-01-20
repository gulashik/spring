package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class ReplayExample {
    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        replay();
    }

    private static void replay() throws InterruptedException {

        // todo Тип ConnectableFlux - Hot Flux
        ConnectableFlux<Integer> replay = Flux.range(1,10)
            .delayElements(Duration.ofMillis(100))
            .doOnNext(integer -> System.out.println("Emitted " + integer))
            // todo replay - преобразует в HOT Publisher и кэширует X последних элементов, согласно настройкам
            // todo replay - Turn this Flux into a CONNECTABLE HOT SOURCE and cache last emitted signals for further Subscriber.
            .replay(2) // todo 2 последних элемента кэшируются
            //.replay(/*default Integer.MAX_VALUE*/) // todo Integer.MAX_VALUE последних элемента кэшируются
            //.replay(2, Duration.ofMillis(200));  // Сохраняем 2 элемента на 200 миллисекунд
            ;

        // todo Стартуем любым способом Hot Publisher
        replay.connect();

        // todo Ожидание чтобы подписчики начали не сначала
        Thread.sleep(500);

        // Первый подписчик
        Disposable disposable = replay
            .doFirst(() -> System.out.println("First subscriber start and get replay count events"))
            .doOnNext(i -> System.out.println("--1 subscriber: " + i))
            .subscribe();

        Thread.sleep(400);

        // Второй подписчик
        Disposable disposable2 = replay
            .doFirst(() -> System.out.println("Second subscriber start and get replay count events"))
            .doOnNext(i -> System.out.println("**2 subscriber: " + i))
            .subscribe();

        waitForDisposableEnd(List.of(disposable, disposable2));
        /* todo элементы ни кто не получил
            Emitted 1
            Emitted 2
            Emitted 3
            Emitted 4
            First subscriber start and get replay count events
            --1 subscriber: 3 (получил по replay)
            --1 subscriber: 4 (получил по replay)
            Emitted 5
            --1 subscriber: 5
            Emitted 6
            --1 subscriber: 6
            Emitted 7
            --1 subscriber: 7
            Emitted 8
            --1 subscriber: 8
            Second subscriber start and get replay count events
            **2 subscriber: 7 (получил по replay)
            **2 subscriber: 8 (получил по replay)
            Emitted 9
            --1 subscriber: 9
            **2 subscriber: 9
            Emitted 10
            --1 subscriber: 10
            **2 subscriber: 10
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
