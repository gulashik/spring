package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class ParallelAndSequenceExample {

    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        parallelAndSequential();

    }

    private static void parallelAndSequential() {

        Disposable disposable = Flux.range(1, 10)
            .log()
            // todo Параллелим поток в несколько параллельных потоков
            .parallel(4) .runOn(Schedulers.parallel())
            .map(
                i -> {
                    System.out.println("Processing " + i + " on thread: " + Thread.currentThread().getName());
                    return i;
                }
            )
            // todo Возвращение потока в последовательный режим
            .sequential()
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
