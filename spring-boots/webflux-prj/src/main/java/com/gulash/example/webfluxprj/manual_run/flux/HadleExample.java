package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.util.List;

public class HadleExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Flux<Integer> flux = Flux.just(1, 2, 3, 4)
            // todo handle - позволяет модифицировать или фильтровать элементы(несколько методов в одном)
            // todo handle — это наиболее гибкий метод, который позволяет контролировать, какие элементы передаются дальше в поток и как они обрабатываются.
            .handle(
                (   // todo данные
                    Integer value,
                    // todo SynchronousSink — это специальный класс, используемый для добавления значений в реактивный поток данных.
                    SynchronousSink<Integer> sink
                ) -> {

                    if (value % 2 == 0) {
                        sink.next(value * 2);  // todo next - эммитим только четные числа
                    }

                })
            .doOnTerminate(() -> System.out.println("Processing complete"));


        flux.subscribe(System.out::println);
        // Выводит: 4, 8
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
