package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class LimitExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Disposable disposable = Flux.range(1, 20)
            .log()
            // todo limitRate(x) - заставляет подписчика запрашивать указанный объём данных за раз и ТОЛЬКО ПОСЛЕ обработки текущего объёма элементов будет следующий запрос
            // todo полезно Когда источник генерирует данные слишком быстро, limitRate помогает уменьшить объем одновременной обработки.
            // todo Помогает предотвратить перегрузку системы
            // todo запрос ОТ и ДО элементов за раз, ТОЛЬКО ПОСЛЕ обработки текущего объёма элементов
            .limitRate(5, 10) // Ограничивает потребление до 5 элементов за раз
            // todo ДО элементов за раз, ТОЛЬКО ПОСЛЕ обработки текущего объёма элементов
            .limitRate(5) // Ограничивает потребление до 5 элементов за раз
            .map(
                i -> {
                    try {
                        System.out.println(Thread.currentThread().getName() + " " + i);
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return i;
                }
            )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        /*
            в логах видим запрос элементов из Upstream
            INFO reactor.Flux.Range.1 -- | request(5)
            ...onNext()
            ...onNext()
            ...onNext()
            ...onNext()
            ...onNext()
             INFO reactor.Flux.Range.1 -- | request(4)
            ...onNext()
            ...
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
