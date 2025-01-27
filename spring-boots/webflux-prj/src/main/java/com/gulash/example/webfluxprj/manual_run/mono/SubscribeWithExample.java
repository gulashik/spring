package com.gulash.example.webfluxprj.manual_run.mono;

import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SubscribeWithExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {

        // todo subscribeWith - используется для создания кастомной подписки.
        // todo необходимо явно завершить DISPOSE()
        CustomerSubscriber subscriber = Mono.just(1)
            .doOnNext(integer -> System.out.println(integer))
            .subscribeOn(Schedulers.parallel())
            .subscribeWith(new CustomerSubscriber());// todo использование

        /*
            1
            Subscriber onNext: 1
            Subscriber onComplete
        */

        // todo ожидалка окончания Disposable - как то кривовато
        while (!subscriber.isDisposed()) {}
    }

}
// todo Есть больше методов
class CustomerSubscriber extends BaseSubscriber<Integer> {
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnNext(Integer value) {
        System.out.println("Subscriber onNext: " + value);
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("Subscriber onComplete");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.out.println("Subscriber onError: " + throwable);
    }

    @Override
    protected void hookOnCancel() {
        System.out.println("Subscriber onCancel");
    }

    @Override
    protected void hookFinally(SignalType type) {
        System.out.println("Subscriber onFinally: " + type);
        dispose(); // todo закрываемся при завершении
    }
}