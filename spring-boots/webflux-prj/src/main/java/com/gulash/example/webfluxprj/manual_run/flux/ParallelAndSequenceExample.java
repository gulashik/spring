package com.gulash.example.webfluxprj.manual_run.flux;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
public class ParallelAndSequenceExample {

    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        parallelAndSequential();

    }

    private static void parallelAndSequential() {

        Disposable disposable = Flux.range(1, 10)
            .parallel(4) // Перевод Flux в параллельный режим
            .runOn(Schedulers.parallel()) // Выполнение на общем параллельном Scheduler
            .map(i -> i * 2)
            .sequential() // Возвращение в последовательный режим
            .subscribe(System.out::println);

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
