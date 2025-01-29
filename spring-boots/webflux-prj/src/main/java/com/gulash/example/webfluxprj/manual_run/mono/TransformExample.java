package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
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
        Mono<String> source = Mono.just("One");

        Mono<String> transformMethod = source
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transform - трансформация на вход Mono<X> на выход Mono
            // todo Создание объекта сразу ПРИ СОЗДАНИИ Mono
            .transform(
                (Mono<String> flux) -> {
                    System.out.println("Создание обертки сразу ПРИ СОЗДАНИИ Mono");
                    return flux.map(String::toUpperCase);
                }
            );
        System.out.println("before call subscribe");

        transformMethod.subscribe(System.out::println);
        /*  -- пример transform
            Создание обертки сразу ПРИ СОЗДАНИИ Mono
            before call subscribe
            subscribing
            ONE
            -- пример transformDeferred
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            ONE
            -- пример transformDeferredContextual
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            from context: 1 from context
            Done
        */
    }

    private static void transformDeferred() {
        Mono<String> source = Mono.just("One");

        Mono<String> transformDeferred = source
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transformDeferred - трансформация на вход Mono<X> на выход Mono
            //  объект Создаётся ОТЛОЖЕНО ПРИ выполнении SUBSCRIBE
            .transformDeferred(
                (Mono<String> flux) -> {
                    System.out.println("Создание обертки ПРИ выполнении subscribe");
                    return flux.map(String::toUpperCase);
                }
            );
        System.out.println("before call subscribe");

        transformDeferred.subscribe(System.out::println);
        /*  -- пример transform
            Создание обертки сразу ПРИ СОЗДАНИИ Mono
            before call subscribe
            subscribing
            ONE
            -- пример transformDeferred
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            ONE
            -- пример transformDeferredContextual
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            from context: 1 from context
            Done
        */
    }

    private static void transformDeferredContextual() {
        Mono<Integer> numbers = Mono.just(1);

        Mono<String> result = numbers
            .doOnSubscribe(subscription -> System.out.println("subscribing"))
            // todo transformDeferredContextual - как transformDeferred + на вход получаем контекст
            .transformDeferredContextual(
                (Mono<Integer> flux, ContextView ctx) -> {
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
        /*  -- пример transform
            Создание обертки сразу ПРИ СОЗДАНИИ Mono
            before call subscribe
            subscribing
            ONE
            -- пример transformDeferred
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            ONE
            -- пример transformDeferredContextual
            before call subscribe
            Создание обертки ПРИ выполнении subscribe
            subscribing
            from context: 1 from context
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