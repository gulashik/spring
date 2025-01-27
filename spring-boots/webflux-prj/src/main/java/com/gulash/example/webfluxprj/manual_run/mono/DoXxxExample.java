package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class DoXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        Disposable disposable = Mono.just("Hello")
            // todo doFirst - вызывается перед выполнением всех остальных операций.
            .doFirst(() -> System.out.println("doFirst"))

            // todo doOnRequest - подписчик запрашивает данные из потока
            .doOnRequest(request -> System.out.println("doOnRequest Запрос на получение данных: " + request))

            // todo doOnSubscribe - вызывается при подписке на поток, до того как любые другие действия начнут выполняться.
            .doOnSubscribe(subscription -> System.out.println("doOnSubscribe Подписка на поток началась"))

            // todo doOnNext - выполнить действие каждый раз, когда в потоке появляется новый элемент.
            .doOnNext(s -> System.out.println("doOnNext: " + s))
            // todo doOnEach - вызывается для каждого сигнала (Next, Complete, Error)
            .doOnEach(signal -> System.out.println("doOnEach Сигнал: " + signal))

            // todo doOnError - действие, когда поток завершился с ошибкой.
            .doOnError(error -> System.out.println("doOnError Произошла ошибка: " + error.getMessage()))
            .doOnError(
                RuntimeException.class,
                error -> System.out.println("doOnError Произошла ошибка: " + error.getMessage())
            )

            // todo doOnCancel - выполнения действий, когда подписчик отменяет поток, например освобождать ресурсы или логировать событие
            //  Отмена подписки - dispose() или .take(X) и т.д. обработка прекращается, и doOnCancel срабатывает.
            .doOnCancel(() -> System.out.println("doOnCancel"))

            // todo doOnDiscard - когда элемент был "отклонен" при вызове take, filter и т.д.
            // .filter(s -> false) // Элемент сброшен: Hello
            .doOnDiscard(String.class, value -> System.out.println("Элемент сброшен: " + value))

            // todo doOnSuccess - когда поток завершился успешно(без ошибок).
            .doOnSuccess(value -> System.out.println("doOnSuccess Поток завершился успешно: " + value))

            // todo doOnTerminate - вызывается как при УСПЕШНОМ завершении потока, так и при завершении С ОШИБКОЙ, НО НЕ при отмене подписки.
            .doOnTerminate(() -> System.out.println("doOnTerminate Завершение потока"))

            // todo doAfterTerminate похоже на doOnTerminate, но с одной важной разницей:
            //      действия, указанные в doAfterTerminate, выполняются после вызова обработчиков завершения (onComplete или onError).
            //      Не срабатывает при отмене подписки
            .doAfterTerminate(() -> System.out.println("doAfterTerminate"))

            // todo doFinally - после завершения потока (успешного или с ошибкой), но в отличие от doAfterTerminate, может обрабатывать сигналы завершения
            .doFinally(signalType -> System.out.println("doFinally"))

            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(s -> System.out.println("subscribe: " + s));

        waitForDisposableEnd(List.of(disposable));
        /*
            doFirst
            doOnSubscribe Подписка на поток началась
            doOnRequest Запрос на получение данных: 9223372036854775807
            doOnNext: Hello
            doOnEach Сигнал: doOnEach_onNext(Hello)
            doOnEach Сигнал: onComplete()
            doOnSuccess Поток завершился успешно: Hello
            doOnTerminate Завершение потока
            subscribe: Hello
            doAfterTerminate
            doFinally
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
