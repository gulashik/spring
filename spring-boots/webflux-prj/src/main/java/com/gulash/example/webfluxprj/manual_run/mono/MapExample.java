package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class MapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        map();
        mapNotNull();
    }

    private static void map() {
        Mono<String> mapped = Mono.just("Aaa")
            // todo map - применяет функцию к элементу, если функция вернёт null, то будет ошибка
            .map(String::toUpperCase)
            //.map(null) // будет ошибка
            ;

        Disposable disposable = mapped
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);
        // AAA

        waitForDisposableEnd(List.of(disposable));
    }
    private static void mapNotNull() {
        Mono<String> mapped = Mono.just("Aaa")
            // todo mapNotNull - как map, на фильтрует null значения при вычислении функции в map
            //.mapNotNull(String::toUpperCase)
            .mapNotNull(null) // ошибки не будет
            ;

        Disposable disposable = mapped
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);
        // ничего не вернётся

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
