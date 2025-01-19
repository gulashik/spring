package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class CacheExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        cache();
        cacheDelay();
    }

    private static void cache() {

        Flux<Integer> rangeFlux = Flux.just(1, 2, 3)
            .map(integer -> getValueWithDelay(integer, "upstream will cache", 100L))
            // todo cache - кэширует X ПОСЛЕДНИХ элементов ВЫШЕ по потоку ПРИ ПЕРВОМ вычислении и ТОЛЬКО сколько ЗАКЕШИРОВАННО столько и будет ДОСТУПНО для ПОСЛЕДУЮЩИХ.
            .cache(1) // todo только 1 последний элемент будет доступен остальным подписчокам
            //.cache() // todo default Integer.MAX_VALUE элементов будет доступен остальным подписчокам
            .map(integer -> getValueWithDelay(integer, "downstream will not cache", 100L))
            ;

        Disposable disposable =  rangeFlux
            .doFirst(() -> System.out.println("******First subscriber calc ALL and CACHE X events******"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));

        Disposable disposable2 =  rangeFlux
            .doFirst(() -> System.out.println("******Second subscriber get ONLY cached events******"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable2));
        /* todo для .cache(1) -  только 1 последний элемент будет доступен остальным подписчокам
            ******First subscriber calc ALL and CACHE X events******
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            1
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            2
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            3
            ******Second subscriber get ONLY cached events******
            Calling getValueWithDelay with tag: downstream will not cache
            3

           todo для .cache() - default Integer.MAX_VALUE элементов будет доступен остальным подписчокам
            ******First subscriber calc ALL and CACHE X events******
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            1
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            2
            Calling getValueWithDelay with tag: upstream will cache
            Calling getValueWithDelay with tag: downstream will not cache
            3
            ******Second subscriber get ONLY cached events******
            Calling getValueWithDelay with tag: downstream will not cache
            1
            Calling getValueWithDelay with tag: downstream will not cache
            2
            Calling getValueWithDelay with tag: downstream will not cache
            3
         */

    }
    private static void cacheDelay() {

        Flux<Integer> rangeFlux = Flux.just(1, 2, 3)
            .map(integer -> getValueWithDelay(integer, "upstream will cache", 30L))
            // todo cache - кэширует X ПОСЛЕДНИХ элементов ВЫШЕ по потоку ПРИ ПЕРВОМ вычислении и ТОЛЬКО сколько ЗАКЕШИРОВАННО столько и будет ДОСТУПНО для ПОСЛЕДУЮЩИХ.
            .cache(5, Duration.ofMillis(40)) // todo ttl - время жизни кэша
            ;

        Disposable disposable =  rangeFlux
            .doFirst(() -> System.out.println("******First subscriber calc ALL and CACHE X events******"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));

        Disposable disposable2 =  rangeFlux
            .doFirst(() -> System.out.println("******Second subscriber get ONLY cached events******"))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable2));
        /* todo
            Calling getValueWithDelay with tag: upstream will cache
            1
            Calling getValueWithDelay with tag: upstream will cache
            2
            Calling getValueWithDelay with tag: upstream will cache
            3
            ******Second subscriber get ONLY cached events******
            2
            3
         */
    }

    private static Integer getValueWithDelay(Integer i, String tag, Long delay) {
        System.out.println("Calling getValueWithDelay with tag: " + tag);
        try { Thread.sleep(delay);} catch (Throwable throwable) {
            throw new RuntimeException("Error getting value", throwable);
        }
        return i;
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
