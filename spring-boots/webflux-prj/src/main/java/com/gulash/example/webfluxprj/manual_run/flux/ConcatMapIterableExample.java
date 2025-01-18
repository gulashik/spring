package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class ConcatMapIterableExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {

        Flux<UserConcat> flux = Flux.just(new UserConcat(1, "Name1"), new UserConcat(2, "Name2"));

        // todo Отличия concatMapIterable и flatMapIterable
        //  todo concatMapIterable - обрабатывает каждый элемент последовательно
        //  todo flatMapIterable - обрабатывает элементы параллельно

        Flux<String> concated = flux
            .doOnNext(userConcat -> System.out.println("Было:" + userConcat))
            // todo concatMapIterable - переобразование из Объекта в Коллекцию
        .concatMapIterable(
                userConcat -> {
                    List<String> list = List.of(String.valueOf(userConcat.id()), userConcat.name());
                    System.out.println("Стало:" + list);
                    return list;
                }
        );

        concated.subscribe(s -> System.out.println("Текущий элемент: " + s));
        /*
            Было:UserConcat[id=1, name=Name1]
            Стало:[1, Name1]
            Текущий элемент: 1
            Текущий элемент: Name1
            Было:UserConcat[id=2, name=Name2]
            Стало:[2, Name2]
            Текущий элемент: 2
            Текущий элемент: Name2
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

record UserConcat(int id, String name) {}
