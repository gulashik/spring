package com.gulash.example.webfluxprj.manual_run.flux.init.publish_subscribe_swtich;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

public class SubscribeWithExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        // todo subscribeWith - используется для создания кастомной подписки.
        // todo необходимо явно завершить DISPOSE()
        CustomerSubscriber subscriber = Flux.range(1, 3)
            .doOnNext(integer -> System.out.println(integer))
            .subscribeOn(Schedulers.parallel())
            .subscribeWith(new CustomerSubscriber());// todo используем

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