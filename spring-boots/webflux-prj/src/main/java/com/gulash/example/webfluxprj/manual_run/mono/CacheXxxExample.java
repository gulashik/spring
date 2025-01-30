package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class CacheXxxExample {
    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        cache();
        cacheTtl();
        cacheDifferentSates();
    }

    private static void cache() {
        Mono<String> mono = Mono.defer(
                () -> {
                    System.out.println("*** Called Supplier ***");
                    return Mono.just("called by Supplier");
                }
            )
            // todo cache - кэширует элемент ВЫШЕ по потоку ПРИ ПЕРВОМ вычислении и далее возвращает без вычисления
            .cache()
            ;

        Disposable disposable = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        //------

        Disposable disposable2 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable2));
        //------

        Disposable disposable3 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable3));
        /* Вызов С cache кеширование случилось
        *** Called Supplier ***
        called by Supplier
        called by Supplier
        called by Supplier
        *
        Вызов БЕЗ cache нет кеширования
        *** Called Supplier ***
        called by Supplier
        *** Called Supplier ***
        called by Supplier
        *** Called Supplier ***
        called by Supplier
        */
    }

    private static void cacheTtl() throws InterruptedException {
        Mono<String> mono = Mono.defer(
                () -> {
                    System.out.println("*** Called Supplier ***");
                    return Mono.just("called by Supplier");
                }
            )
            // todo cache - кэширует элемент ВЫШЕ по потоку ПРИ ПЕРВОМ вычислении и далее возвращает без вычисления, но с учётом timeout.
            .cache(Duration.ofSeconds(1)/* default, Schedulers.parallel()*/);


        Disposable disposable = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        Thread.sleep(800);
        //------


        Disposable disposable2 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable2));
        Thread.sleep(800);
        //------

        Disposable disposable3 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable3));

        /* timeout прошёл и был второй вызов
            *** Called Supplier ***
            called by Supplier
            called by Supplier
            *** Called Supplier ***
            called by Supplier
        */
    }

    private static void cacheDifferentSates() throws InterruptedException {
        Mono<String> mono = Mono.defer(
                () -> {
                    System.out.println("*** Called Supplier ***");
                    return Mono.just("called by Supplier");
                }
            )
            // todo cache - кэширует элемент ВЫШЕ по потоку ПРИ ПЕРВОМ вычислении и далее возвращает без вычисления, но с учётом timeout.
            .cache(
                s -> Duration.ofSeconds(1), // todo Вычисленное значение будет кэшироваться на
                throwable -> Duration.ofSeconds(2), // todo Ошибка будет кэшироваться на
                () -> Duration.ofSeconds(1) // todo Пустое значение будет кэшироваться на
            );

        Disposable disposable = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        Thread.sleep(800);
        //------

        Disposable disposable2 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable2));
        Thread.sleep(800);
        //------

        Disposable disposable3 = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable3));

        /* timeout прошёл и был второй вызов
            *** Called Supplier ***
            called by Supplier
            called by Supplier
            *** Called Supplier ***
            called by Supplier
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
