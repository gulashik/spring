package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.List;

public class DoXxxAndOnXxxExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        manyMethods();
        onErrorContinue();
    }

    private static void onErrorContinue() {
        try {
            Flux<Integer> flux = Flux.just("1", "2", "a", "3")
                .publishOn(Schedulers.boundedElastic())
                .doFirst(() -> System.out.println("onErrorContinue(Throwable throwable, Object object)"))
                .map(Integer::parseInt)
                // todo onErrorContinue - при возникновении ошибки делает возможным продолжить поток
                .onErrorContinue(
                    (Throwable throwable, Object object) -> {
                        System.out.println("Error object: " + object);
                        throwable.printStackTrace();
                    }
                );

            Disposable disposable = flux.subscribe(System.out::println);

            waitForDisposableEnd(List.of(disposable));
        } finally {}

        try {
            Flux<Integer> flux = Flux.just("1", "2", "a", "3")
                .publishOn(Schedulers.boundedElastic())
                .doFirst(() -> System.out.println("onErrorContinue(Class.class, Throwable throwable, Object object)"))
                .map(Integer::parseInt)
                // todo onErrorContinue - срабатываем по классу ошибки ЕСЛИ ОШИБКА ОТЛИЧАЕТСЯ - ВТИХУЮ ЗАВЕШАЕМ ПОТОК
                .onErrorContinue(
                    NumberFormatException.class/* NumberFormatException.class*//*IllegalAccessError.class*/, // todo ЕСЛИ ОШИБКА ОТЛИЧАЕТСЯ - ЗАВЕШАЕМ ПОТОК // Класс ошибки
                    (Throwable throwable, Object object) -> {
                        System.out.println("1 Error object: " + object);
                        throwable.printStackTrace();
                    }
                )                ;

            Disposable disposable = flux.subscribe(System.out::println);

            waitForDisposableEnd(List.of(disposable));
        } finally {}

        try {
            Flux<Integer> flux = Flux.just("1", "2", "a", "3")
                .publishOn(Schedulers.boundedElastic())
                .doFirst(() -> System.out.println("onErrorContinue(Predicate, Throwable throwable, Object object)"))
                .map(Integer::parseInt)
                // todo onErrorContinue - ищем по Predicate из класса ошибки, если ОШИБКА не отловлена ВТИХУЮ ЗАВЕШАЕМ ПОТОК
                .onErrorContinue(
                    throwable -> throwable instanceof RuntimeException, // todo Predicate по классу ошибки
                    (Throwable throwable, Object object) -> {
                        System.out.println("2 Error object: " + object);
                        throwable.printStackTrace();
                    }
                )
                ;

            Disposable disposable = flux.subscribe(System.out::println);

            waitForDisposableEnd(List.of(disposable));
        } finally {}

    }


    private static void manyMethods() {

        Flux<Integer> flux = Flux.range(1, 10)
            // todo Flux.error - добавляем ошибку в потока чаще нужно для тестирования
            .concatWith(Flux.error(RuntimeException::new)) // генерируем ошибку

            // todo ==========================================================================
            // todo  doFirst - для подготовки перед выполнением реактивного потока.
            // todo  несколько doFirst, они выполняются в обратном порядке (LIFO — “последний пришел, первый выполнен”).
            .doFirst(() -> System.out.println("doFirst step"))

            // todo ==========================================================================
            // todo doOnSubscribe выполняется до начала выполнения потока, но после вызова метода subscribe.
            .doOnSubscribe(
                subscription -> {
                    System.out.println("doOnSubscribe step");
                }
            )

            // todo ==========================================================================
            // todo doOnEach - получаем сигнал(содержит разные данные)
            .doOnEach(
                signal -> {
                            // todo сами данные
                            Integer i = signal.get();
                            System.out.println("doOnEach. current item: " + i);

                            // todo read-only context
                            String ctxValue = signal.getContextView().getOrDefault("key1", "no_key");
                            System.out.println("ctxValue: " + ctxValue);
                        }
            )

            // todo ==========================================================================
            // todo doOnNext - получаем данные из сигнала
            .doOnNext(integer -> System.out.println("doOnNext. current item: " + integer))

            // todo ==========================================================================
            // todo doOnDiscard - для выполнения действий при удалении элемента из потока, который больше не будет обработан в цепочке.
            .filter(i -> i % 2 == 0) // Оставляем только чётные числа
            .doOnDiscard(Integer.class, i -> System.out.println("doOnDiscard. Отброшено: " + i))

            // todo ==========================================================================
            // todo doOnError срабатывает на любое исключение внутри потока, но не предотвращает его распространение.
            .doOnError(
                RuntimeException.class,
                exception -> {
                    System.out.println("doOnError with explicit Class step");
                    // закрываем ресурсы и т.д.
                }
            )
            .doOnError(
                exception -> {
                    System.out.println("doOnError without explicit Class step");
                    // закрываем ресурсы и т.д.
                }
            )
            // ошибка если нужно
            //.map(integer -> integer > 2 ? 1/0 : integer)

            // todo ==========================================================================
            // todo onErrorResume - при ошибке возобновляем
            .onErrorResume(
                RuntimeException.class,
                exception -> Flux.just(10, 20, 30) // todo при ошибке возобновляемся
            )
            // todo onErrorReturn - перехватывает ошибку и заменяет её указанным значением.
            .onErrorReturn(-1) // todo что возвращается при ошибке

            // todo onErrorComplete - завершаемся при ТАКОМ типе ошибки
            .onErrorComplete(RuntimeException.class) //todo при ошибке завершаемся
            // todo onErrorComplete - завершаемся при ЛЮБОМ типе ошибки
            .onErrorComplete() //todo при ошибке завершаемся

            // todo ==========================================================================
            // todo doOnCancel - выполнения действий, когда подписчик отменяет поток, например освобождать ресурсы или логировать событие
            //  Отмена подписки - dispose() или .take(X) и т.д. обработка прекращается, и doOnCancel срабатывает.
            .doOnCancel(
                () -> System.out.println("doOnCancel step")
            )

            // todo ==========================================================================
            // todo doAfterTerminate похоже на doOnTerminate, но с одной важной разницей:
            //      действия, указанные в doAfterTerminate, выполняются после вызова обработчиков завершения (onComplete или onError).
            //      Не срабатывает при отмене подписки
            .doAfterTerminate(() -> System.out.println("doAfterTerminate step. Execute after onComplete или onError"))

            // todo ==========================================================================
            // todo doOnComplete - действий в момент завершения потока без ошибок.
            .doOnComplete(() -> System.out.println("doOnComplete"))

            // todo ==========================================================================
            // todo doOnTerminate - вызывается как при УСПЕШНОМ завершении потока, так и при завершении С ОШИБКОЙ, НО НЕ при отмене подписки.
            .doOnTerminate(
                () -> System.out.println("doOnTerminate step. Поток завершён (успех или ошибка)")
            )
            // todo ==========================================================================
            // todo doFinally - после завершения потока
            .doFinally(
                signalType -> {
                    System.out.println("doFinally. current state: " + signalType.name());
                }
            )
            ;

        Disposable disposable = flux.subscribe(
            integer -> System.out.println("from subscribe: " + integer),
            throwable -> {
                throw new RuntimeException("Some exception");
            },
            () -> System.out.println("Done from subscribe"),
            Context.of("key1", "Value1")
        );

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
