package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Timed;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConcatWithExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Mono<String> mono1 = Mono.just("Hello").delayElement(Duration.ofMillis(1000));
        Mono<String> mono2 = Mono.just("World");

        // todo concatWith - создаёт Flux из двух Mono последовательно
        //  применяется ПОСЛЕДОВАТЕЛЬНО как цепочка асинхронных операций, чтобы одна начиналась только после завершения другой.
        Flux<String> result = mono1.concatWith(mono2);

        Disposable disposable = result
            .timed()
            .subscribe(
                (Timed<String> stringTimed) -> System.out.println(stringTimed.get() + " " + stringTimed.elapsed())
            );
        // Вывод: Hello (через 1 секунду) World
        /*
            Hello PT1.002021416S
            World PT0.002405959S
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

    private static String formatDuration(Duration duration) {
        LocalTime time = LocalTime.ofNanoOfDay(duration.getNano());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'часов-'H 'минут-'m 'секунд-'s.SSS");
        return "Прошло(" + time.format(formatter)+")";
    }
}
