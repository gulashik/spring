package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class ZipExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        zipWith();
        zipWithIterable();
    }

    private static void zipWith() {
        Flux<String> names = Flux.just("Alice", "Bob", "Charlie", "Max").delayElements(Duration.ofMillis(100));
        Flux<Integer> scores = Flux.just(90, 85, 78).delayElements(Duration.ofMillis(500));

        Flux<String> result = names
            .doFirst(() -> System.out.println("Комбинирует текущий поток с другим потоком"))
            //todo zipWith - комбинирует текущий поток с другим потоком
            //  По наименьшему по длине потоку
            //  Работает с ожиданиеи "один к одному": берется один элемент из каждого потока и применяется функция объединения.
            .zipWith(scores, (name, score) -> name + " scored " + score);

        Disposable disposable = result.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Комбинирует текущий поток с другим потоком
            Alice scored 90
            Bob scored 85
            Charlie scored 78
        */
    }

    private static void zipWithIterable() {
        Flux<String> flux = Flux.just("Alice", "Bob", "Charlie", "Max").delayElements(Duration.ofMillis(100));
        List<Integer> list = List.of(90, 85, 78);

        Disposable disposable = flux
            .doFirst(() -> System.out.println("Комбинирует текущий Поток с Коллекций"))
        // todo zipWithIterable - позволяет объединить поток с коллекцией (Iterable) на основе заданной функции.
        //  По наименьшему по длине потоку
        //  Работает с ожиданиеи "один к одному": берется один элемент из каждого потока и применяется функция объединения.
            .zipWithIterable(
                list,
                (fluxItem, lstItem) -> "fluxItem - %s, lstItem - %s".formatted(fluxItem, lstItem)
            )
            .subscribe(System.out::println);


        waitForDisposableEnd(List.of(disposable));
        /*
            Комбинирует текущий Поток с Коллекций
            fluxItem - Alice, lstItem - 90
            fluxItem - Bob, lstItem - 85
            fluxItem - Charlie, lstItem - 78
         */
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
