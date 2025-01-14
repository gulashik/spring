package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;

public class TransformExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        transform();
        transformDeferred();
        transformDeferredContextual();
    }

    private static void transform() {
        Flux<String> source = Flux.just("One", "Two", "Three", "Four", "Five");

        Flux<String> transformMethod = source
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transform - трансформация на вход Flux<X> на выход Flux/Mono
            // todo Создание объекта сразу ПРИ СОЗДАНИИ Flux
            .transform(
                (Flux<String> flux) -> {
                    System.out.println("Создание обертки сразу ПРИ СОЗДАНИИ Flux");
                    return flux.map(String::toUpperCase);
                }
            );
        System.out.println("before call subscribe");

        transformMethod.subscribe(System.out::println);
        /*  ---Вывод из примера по "transform"---
            Создание обертки сразу ПРИ СОЗДАНИИ Flux
            before call subscribe
            subscribing
            ONE
            TWO
            THREE
            FOUR
            FIVE
            ---Вывод из примера по "transformDeferred"---
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            ONE
            TWO
            THREE
            FOUR
            FIVE
        */
    }

    private static void transformDeferred() {
        Flux<String> source = Flux.just("One", "Two", "Three", "Four", "Five");

        Flux<String> transformDeferred = source
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transformDeferred - трансформация на вход Flux<X> на выход Flux/Mono
            //  объект Создаётся ОТЛОЖЕНО ПРИ выполнении SUBSCRIBE
            .transformDeferred(
                (Flux<String> flux) -> {
                    System.out.println("Создание обертки ПРИ выполнении subscribe");
                    return flux.map(String::toUpperCase);
                }
            );
        System.out.println("before call subscribe");

        transformDeferred.subscribe(System.out::println);
        /*  ---Вывод из примера по "transform"---
            Создание обертки сразу ПРИ СОЗДАНИИ Flux
            before call subscribe
            subscribing
            ONE
            TWO
            THREE
            FOUR
            FIVE
            ---Вывод из примера по "transformDeferred"---
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            ONE
            TWO
            THREE
            FOUR
            FIVE
        */
    }

    private static void transformDeferredContextual() {
        Flux<Integer> numbers = Flux.range(1, 5);

        Flux<String> result = numbers
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transformDeferredContextual - как transformDeferred + на вход получаем контекст
            .transformDeferredContextual(
                (Flux<Integer> flux, ContextView ctx) -> {
                    // todo получаем из контекста
                    String prefix = ctx.getOrDefault("prefix", "Number: ");
                    String suffix = ctx.getOrDefault("suffix", ".");

                    System.out.println("Создание обертки ПРИ выполнении subscribe");
                    return flux.map(data -> prefix + data + suffix);
                }
            )
            // todo кладём в контекст
            .contextWrite(Context.of("prefix", "from context: "));

        System.out.println("before call subscribe");

        result.subscribe(
            System.out::println,
            Throwable::printStackTrace,
            () -> System.out.println("Done"),
            Context.of("suffix", " from context") // todo кладём в контекст
        );
        /*
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            from context: 1 from context
            from context: 2 from context
            from context: 3 from context
            from context: 4 from context
            from context: 5 from context
            Done
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

