package com.gulash.example.webfluxprj.manual_run.flux.init.cold;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

public class GenerateExample {
    public static void main(String[] args) {
        // todo Flux.generate позволяет СИНХРОННО генерировать элементы по одному с использованием состояния(SynchronousSink)
        Flux<Integer> generate = Flux.generate(
            () -> 0, // initial state
            (Integer currentState, SynchronousSink<Integer> synchronousSink) -> {
                // todo что-то отдали в поток
                synchronousSink.next(currentState);

                // Exception
                // synchronousSink.error(new RuntimeException("something went wrong"));

                // todo complete - завершаемся когда нужно
                if (currentState == 10) {
                    synchronousSink.complete();
                }
                // todo передали на следующую итерацию
                return currentState + 1;
            }
        );

        generate
            .subscribe(System.out::println);
    }
}
