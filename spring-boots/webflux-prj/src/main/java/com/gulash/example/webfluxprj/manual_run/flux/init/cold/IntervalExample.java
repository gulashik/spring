package com.gulash.example.webfluxprj.manual_run.flux.init.cold;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class IntervalExample {
    public static void main(String[] args) {
        // todo Flux.interval - для генерации бесконечной последовательности типа Long c нуля с указанными интервалами времени
        Flux<Long> interval = Flux.interval(
                Duration.ofSeconds(1), // initial delay
                Duration.ofSeconds(2), // period of increment
                Schedulers.parallel() // Планировщик потоков выполнения
            )
            .take(5);// используем ограничитель

        Disposable disposable = interval.subscribe(
            item -> System.out.println("item: %s %s".formatted(item, LocalDateTime.now().withNano(0)))
        );

        waitForDisposableEnd(List.of(disposable));
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> {
                while (!disposable.isDisposed()) {
                }
            }
        );
    }

}
