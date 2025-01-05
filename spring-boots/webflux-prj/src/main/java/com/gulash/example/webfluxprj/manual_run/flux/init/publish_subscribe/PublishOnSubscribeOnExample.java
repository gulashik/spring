package com.gulash.example.webfluxprj.manual_run.flux.init.publish_subscribe;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class PublishOnSubscribeOnExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        exampleSubscribeOnPublishOn();
    }

    private static void exampleSubscribeOnPublishOn() {

        // todo subscribeOn определяет, где НАЧНЁТСЯ выполнение цепочки
        // todo publishOn определяет, где выполняется СЛЕДУЮЩИЙ ЭТАП обработки данных в цепочке
        //  Избегать частого использования publishOn, так как это может негативно сказаться на производительности из-за переключения контекста.
        Disposable disposable = Flux.range(1, 5)
            .subscribeOn(Schedulers.parallel()) // todo
            .map(i -> {
                System.out.println("Map 1 on thread(будет из subscribeOn): " + Thread.currentThread().getName());
                return i;
            })
            .publishOn(Schedulers.boundedElastic()) // Переключение на другой планировщик
            .map(i -> {
                System.out.println("Map 2 on thread(будет boundedElastic): " + Thread.currentThread().getName());
                return i * 2;
            })
            .publishOn(Schedulers.single()) // Переключение на другой планировщик
            .map(i -> {
                System.out.println("Map 3 on thread(будет single): " + Thread.currentThread().getName());
                return i * 3;
            })
            .subscribe(i -> System.out.println("Subscriber received: " + i + " on thread: " + Thread.currentThread().getName()));

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
