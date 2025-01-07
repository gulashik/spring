package com.gulash.example.webfluxprj.manual_run;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.List;

public class ContextExample {
    public static void main(String[] args) {
        // todo Context — это неизменяемая MAP-а, которую можно использовать для передачи данных(идентификаторы запросов, токены или параметры) по цепочке реактивного программирования.

        Disposable disposable = Flux.range(1, 5)
            // todo Получаем на вход read-only контекст - ОДИН РАЗ запускается
            .transformDeferredContextual(
                (integerFlux, contextView) -> {
                    // todo получаем из контекста
                    String contextWriteVar = contextView.getOrDefault("contextWrite_key", "defWrite_value");
                    System.out.println("contextWriteVar: " + contextWriteVar);

                    // todo проверяем есть ли ключ
                    if (contextView.hasKey("contextWrite_key")) {
                        String someKey = contextView.get("contextWrite_key");
                        System.out.println("contextWrite_key: " + someKey);
                    }

                    // todo выводим всё что есть в контексте
                    contextView.forEach((key, value) -> System.out.println("transformDeferredContextual. key: " + key + " value: " + value));

                    return integerFlux;
                }
            )
            // todo Получаем на вход read-only контекст - НА КАЖДЫЙ СИГНАЛ в потоке запускается
            .doOnEach(
                signal -> {
                    // todo выводим всё что есть в контексте
                    signal.getContextView()
                        .forEach((key, value) -> System.out.println("doOnEach. key: " + key + " value: " + value));
                }
            )
            // todo Получаем на вход read-only контекст - НА КАЖДЫЙ СИГНАЛ в потоке запускается
            .map(
                integer -> {
                    // todo обращаемся к контексту через deferContextual
                    Flux<String> stringFlux = Flux.deferContextual(
                        contextView -> {
                            String contextWriteVar = contextView.getOrDefault("contextWrite_key", "defWrite_value");
                            return Flux.just(contextWriteVar);
                        }
                    );
                    stringFlux
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(value -> System.out.println("Выводим через Mono.deferContextual: " + value));

                    return integer;
                }
            )
            // todo добавляем в контекст. Похоже это нужно делать ниже чтения из контекста
            .contextWrite(Context.of("contextWrite_key", "contextWrite_value"))

            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                value -> System.out.println(value),
                error -> System.err.println(error),
                () -> System.out.println("Done"),
                // todo создаём начальный контекст
                Context.of("subscr_key", "subscr_value")
            );

          waitForDisposableEnd(List.of(disposable));
    }
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> { while (!disposable.isDisposed()) {}}
        );
    }
}
