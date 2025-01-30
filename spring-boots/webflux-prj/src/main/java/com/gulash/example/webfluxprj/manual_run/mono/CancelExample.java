package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CancelExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> mainMono = Mono.just("XXX")
            .doOnNext( value -> System.out.println("Received: %s on %s".formatted(value, Thread.currentThread().getName()) ))
            .delayElement(Duration.ofSeconds(2)) // Задержка в 2 секунд
            .doOnTerminate(() -> System.out.println("Operation terminated on %s".formatted(Thread.currentThread().getName())))
            .subscribeOn(Schedulers.boundedElastic()); // Выполнение на планировщике boundedElastic

        Disposable disposable = mainMono
            // todo cancelOn - создаем Mono на одном потоке, но завершаем на другом
            .cancelOn(Schedulers.parallel()) // Отмена операции на планировщике parallel
            .subscribe();

        waitForDisposableEnd(List.of(disposable));
        // Здесь Mono будет отменено, когда переключится на планировщик parallel
        // Но при этом оно будет работать на boundedElastic, и по прошествии 5 секунд
        // завершится на планировщике boundedElastic, если только планировщик parallel не завершится раньше.
        /*
            Received: XXX on boundedElastic-1
            Operation terminated on parallel-1
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
