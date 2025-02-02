package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DelayUntilExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        delay();
    }

    private static void delay() {
        Mono<Integer> mono = Mono.just(1)
            .doFirst(() -> System.out.println("Начало: " + LocalDateTime.now()))
            // todo Типичные сценарии применения:
            //  Проверка прав доступа перед выполнением операции
            //  Валидация данных
            //  Обновление связанных сущностей
            //  Отправка уведомлений

            // todo delayUntil - отложить эмиссию элемента до окончания работы Publisher-а
            //  В отличие от flatMap, результат функции triggerProvider (например, Mono) не преобразуется в новый элемент потока,
            //  а используется для контроля времени испускания.
            .delayUntil( integer -> {
                    // todo ждём окончания потока
                    return Mono
                        .delay(Duration.ofSeconds(integer))
                        .doFirst(() -> System.out.println("***** delayUntil calling for for: " + integer + " and wait " + integer + " second"));
                }
            )
            .subscribeOn(Schedulers.parallel());

        Disposable disposable = mono.subscribe(i -> System.out.println("get " + i + " at " + LocalDateTime.now()));

        waitForDisposableEnd(List.of(disposable));
        /*
            Начало: 2025-02-02T15:52:24.591190
            ***** delayUntil calling for for: 1 and wait 1 second
            get 1 at 2025-02-02T15:52:25.595744
        */
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> {
                while (!disposable.isDisposed()) {
                }
            }
        );
    }
}
