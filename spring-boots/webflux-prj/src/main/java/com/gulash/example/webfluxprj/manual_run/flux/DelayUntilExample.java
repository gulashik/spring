package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class DelayUntilExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        delay();
    }

    private static void delay() {
        Flux<Integer> flux = Flux.range(1, 5)
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

        Disposable disposable = flux.subscribe(i -> System.out.println("get " + i + " at " + LocalDateTime.now()));

        waitForDisposableEnd(List.of(disposable));
        /*
            Начало: 2025-01-22T18:42:44.280627
            ***** delayUntil calling for for: 1 and wait 1 second
            get 1 at 2025-01-22T18:42:45.306120
            ***** delayUntil calling for for: 2 and wait 2 second
            get 2 at 2025-01-22T18:42:47.312037
            ***** delayUntil calling for for: 3 and wait 3 second
            get 3 at 2025-01-22T18:42:50.317788
            ***** delayUntil calling for for: 4 and wait 4 second
            get 4 at 2025-01-22T18:42:54.321158
            ***** delayUntil calling for for: 5 and wait 5 second
            get 5 at 2025-01-22T18:42:59.327098
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
