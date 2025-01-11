package com.gulash.example.webfluxprj.manual_run.flux.block;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class BlockExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        // Блокирующий вариант
        exampleBlockFirst();
        exampleBlockLast();
        // НЕ блокирующий вариант
        exampleNextLast();
    }
    /*
        НЕ ИСПОЛЬЗУЕМ subscribe

        Применение:
            Применяется в тестах.
            Используется в местах, где требуется интеграция реактивного кода с императивным.
    */
    // todo blockFirst, blockLast - для превращения неблокирующего потока данных (Flux) в синхронное значение.
    private static void exampleBlockFirst() {
        Flux<Integer> integerFlux = Flux.range(1, 10).log();

        System.out.printf("blockFirst start - %s%n", LocalDateTime.now());
        Integer iFirst = integerFlux
            .delayElements(Duration.ofMillis(1000))
            // todo blockFirst - Возвращает ПЕРВЫЙ элемент из потока или блокирует выполнение, пока не появится элемент.
            //  Если Flux пуст, будет выброшено исключение NoSuchElementException.
            .blockFirst();

        System.out.printf("blockFirst - end %s%n", LocalDateTime.now());
        System.out.printf("iFirst %s%n", iFirst);
    }
    private static void exampleBlockLast() {
        Flux<Integer> integerFlux = Flux.range(1, 10).log();

        System.out.printf("blockLast start - %s%n", LocalDateTime.now());
        Integer iFisrt = integerFlux
            .delayElements(Duration.ofMillis(100))
            // todo blockLast - Возвращает ПОСЛЕДНИЙ элемент потока или блокирует выполнение, пока поток не завершится.
            //  Если Flux пуст, будет выброшено исключение NoSuchElementException.
            .blockLast();
        System.out.printf("blockLast - end %s%n", LocalDateTime.now());
        System.out.printf("iLast %s%n", iFisrt);
    }
    private static void exampleNextLast() {
        Flux<Integer> numbers = Flux.just(1, 2, 3, 4).delayElements(Duration.ofMillis(new Random().nextInt(100, 300)));


        // Неблокирующий вариант с использованием first(), next()
        Mono<Integer> firstNumberMono = numbers.next();
        Mono<Integer> lastNumberMono = numbers.last();

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
