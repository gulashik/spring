package com.gulash.example.webfluxprj.manual_run;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SchedulersExample {

    public static void main(String[] args) throws InterruptedException {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
         schedulersAndDisposable();
    }
    private static void schedulersAndDisposable() throws InterruptedException {
/*
    Schedulers — это абстракция для управления потоками и планирования задач(где и как будут выполняться задачи).
    Schedulers играют ключевую роль в управлении потоками выполнения задач. Они определяют, в каком потоке будет выполняться реактивная цепочка или её часть.
        Schedulers.parallel() - Глобальный пул потоков для вычислительных задач (CPU-bound).
        Schedulers.newParallel("worker-thread", 2) - Локальный пул потоков для изолированной параллельной обработки. нужно xxx.dispose();

        Schedulers.boundedElastic() - Гибкий пул потоков для задач, которые могут блокировать (I/O-bound).
        Schedulers.newBoundedElastic(10, 100, "custom-scheduler") -

        Schedulers.single() - Один поток для выполнения задач. Используется для операций, требующих последовательности.

        Schedulers.immediate() - Исполняет задачи немедленно в текущем потоке.

        Schedulers.fromExecutor(Executor) - Позволяет использовать пользовательский пул потоков через ExecutorService.

        Примеры
        subscribeOn(Scheduler) - Определяет, в каком Scheduler начнётся выполнение реактивной цепочки.
        publishOn(Scheduler) - Переключает выполнение цепочки на другой Scheduler.
*/
        Scheduler scheduler =
            //Schedulers.parallel();// для большинства задач, чтобы не управлять потоками вручную.
            Schedulers.boundedElastic(); // для I/O-bound задач
            //Schedulers.newBoundedElastic(/*Максимальное количество потоков*/10, /*Максимальное количество задач в очереди*/100, /*Название пула*/"custom-scheduler"); // для I/O-bound задач
            //Schedulers.newParallel("worker-thread", 2); // нужно scheduler.dispose();

            // не разбирался
            // нужно закрывать пул потоков shutdown()
            // ExecutorService executorService = Executors.newFixedThreadPool(5);
            // Scheduler customScheduler = Schedulers.fromExecutor(executorService);

        Flux<Integer> integerFlux =
            Flux
                .range(1, 5)
                .delayElements(Duration.ofSeconds(1), scheduler);

        Disposable disposable1 = integerFlux
            .subscribeOn(scheduler)
            .subscribe(
                integer -> System.out.println(integer),
                throwable -> { throw new RuntimeException();},
                () -> { System.out.println("Done");}
            );

        Disposable disposable2 = integerFlux
            .subscribeOn(scheduler)
            .doOnNext(integer -> System.out.println(integer))
            .doOnError(throwable -> { throw new RuntimeException();})
            .doOnComplete(() -> { System.out.println("Done");})
            .subscribe();

        waitForDisposableEnd(List.of(disposable1, disposable2));

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
