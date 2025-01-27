package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.observability.SignalListener;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TapExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
            signalListner();
        }

        private static void signalListner() {
            Mono<Integer> mono = Mono.just(1)
                // todo tap - единое API для реализации обработки методов doO... + возможность использовать Context
                // todo tap - как map для методов doO... + возможность использовать Context
                .tap(context -> createSignalListener(context)) // todo реализуем SignalListener
                .contextWrite(context -> context.put("requestId", "contextWrite"))
                .doOnNext(integer -> System.out.println("doOnNext(Method) " + integer)) // todo методы срабатывают оба - не перезаписываются
                ;

            mono.subscribe(
                value -> System.out.println("subscribe(method) Received: " + value),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Stream completed"),
                Context.of("requestId", "subscribe-Context.of")
            );
        /*
            doFirst
            doOnSubscription
            doOnRequest: 9223372036854775807
            doOnNext(SignalListener) onNext: 1
            doOnNext(Method) 1
            subscribe(method) Received: 1
            Stream completed
            onComplete, total elements: 1
            doAfterComplete
            signalType: onComplete
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
                    System.out.println("doOnNext(SignalListener) onNext: " + value);
                    counter.incrementAndGet();
                }

                @Override
                public void doOnComplete() {
                    System.out.println("onComplete, total elements: " + counter.get());
                }

                @Override
                public void doOnError(Throwable throwable) {
                    System.err.println("onError: " + throwable.getMessage());
                }

                @Override
                public void doAfterComplete() throws Throwable {
                    System.out.println("doAfterComplete");
                }

                @Override
                public void doAfterError(Throwable error) throws Throwable {
                    System.out.println("doAfterError: " + error.getMessage());
                }

                @Override
                public void doOnMalformedOnNext(Integer value) throws Throwable {
                    System.out.println("doOnMalformedOnNext: " + value);
                }

                @Override
                public void doOnMalformedOnError(Throwable error) throws Throwable {
                    System.out.println("doOnMalformedOnError: " + error.getMessage());
                }

                @Override
                public void doOnMalformedOnComplete() throws Throwable {
                    System.out.println("doOnMalformedOnComplete");
                }

                @Override
                public void handleListenerError(Throwable listenerError) {
                    System.err.println("handleListenerError: " + listenerError.getMessage());
                }

                @Override
                public Context addToContext(Context originalContext) {
                    return SignalListener.super.addToContext(originalContext);
                }

                @Override
                public void doFirst() throws Throwable {
                    System.out.println("doFirst");
                }

                @Override
                public void doFinally(SignalType signalType) throws Throwable{
                    System.out.println("signalType: " + signalType);
                }

                @Override
                public void doOnSubscription() throws Throwable {
                    System.out.println("doOnSubscription");
                }

                @Override
                public void doOnFusion(int negotiatedFusion) throws Throwable {
                    System.out.println("doOnFusion: " + negotiatedFusion);
                }

                @Override
                public void doOnRequest(long requested) throws Throwable {
                    System.out.println("doOnRequest: " + requested);
                }

                @Override
                public void doOnCancel() throws Throwable {
                    System.out.println("doOnCancel");
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
