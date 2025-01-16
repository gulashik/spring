package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.observability.SignalListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        signalListner();
    }

    private static void signalListner() {
        Flux<Integer> flux = Flux.range(1, 5)
            // todo tap - единое API для реализации обработки методов doO... + возможность использовать Context
            // todo tap - как map для методов doO... + возможность использовать Context
            .tap(context -> createSignalListener(context)) // todo реализуем SignalListener
            .contextWrite(context -> context.put("requestId", "12345"))
            .doOnNext(integer -> System.out.println("doOnNext(Method) " + integer)) // todo методы срабатывают оба - не перезаписываются
            ;

        flux.subscribe(
            value -> System.out.println("subscribe(method) Received: " + value),
            error -> System.err.println("Error: " + error),
            () -> System.out.println("Stream completed")
        );
        /*
            doOnNext(Method) 1
            subscribe(method) Received: 1
            doOnNext(SignalListener) RequestId: 12345, onNext: 1
            ---
            doOnNext(Method) 2
            subscribe(method) Received: 2
            doOnNext(SignalListener) RequestId: 12345, onNext: 2
            ---
            ...
        */
    }

    private static SignalListener<Integer> createSignalListener(ContextView context/*todo Контекст*/) {
        String requestId = context.getOrDefault("requestId", "unknown");
        AtomicLong counter = new AtomicLong();

        // todo SignalListener - обработчики событий для действий doO...
        return new SignalListener<>() {
            @Override
            public void doOnNext(Integer value) {
                // todo методы срабатывают оба - не перезаписываются
                System.out.println("doOnNext(SignalListener) RequestId: " + requestId + ", onNext: " + value);
                System.out.println("---");
                counter.incrementAndGet();
            }

            @Override
            public void doOnComplete() {
                System.out.println("RequestId: " + requestId + ", onComplete, total elements: " + counter.get());
            }

            @Override
            public void doOnError(Throwable throwable) {
                System.err.println("RequestId: " + requestId + ", onError: " + throwable.getMessage());
            }

            @Override
            public void doAfterComplete() throws Throwable {

            }

            @Override
            public void doAfterError(Throwable error) throws Throwable {

            }

            @Override
            public void doOnMalformedOnNext(Integer value) throws Throwable {

            }

            @Override
            public void doOnMalformedOnError(Throwable error) throws Throwable {

            }

            @Override
            public void doOnMalformedOnComplete() throws Throwable {

            }

            @Override
            public void handleListenerError(Throwable listenerError) {

            }

            @Override
            public void doFirst() throws Throwable {

            }

            @Override
            public void doFinally(SignalType signalType) {
                System.out.println("RequestId: " + requestId + ", signalType: " + signalType);
            }

            @Override
            public void doOnSubscription() throws Throwable {

            }

            @Override
            public void doOnFusion(int negotiatedFusion) throws Throwable {

            }

            @Override
            public void doOnRequest(long requested) throws Throwable {

            }

            @Override
            public void doOnCancel() throws Throwable {

            }
        };
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
