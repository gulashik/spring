package com.gulash.example.webfluxprj.manual_run;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.List;

public class CheckpointExample {
    public static void main(String[] args) {
        Flux<Integer> flux = Flux.range(1, 5)
            // todo checkpoint - Включение стека увеличивает объем информации, полезной для отладки, но также может повлиять на производительность.
            .checkpoint("Before map operation")
            .map(i -> {
                if (i == 3) {
                    throw new RuntimeException("Error at " + i);
                }
                return i * 2;
            })
            .checkpoint("After map operation")
         ;

        flux.subscribe(System.out::println, Throwable::printStackTrace);
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
