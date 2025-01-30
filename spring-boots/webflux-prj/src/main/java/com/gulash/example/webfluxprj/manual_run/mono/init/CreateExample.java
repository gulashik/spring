package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

public class CreateExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
    /*
        todo Mono.create используется для ручного создания реактивного потока, который можно наполнять данными вручную через FluxSink.
            Может быть полезен для интеграции с callback-базированными библиотеками.
            Может быть полезен в случаях, когда поток данных не определен на момент его создания, или требуется низкоуровневый контроль.
    */
        Mono<String> mono = Mono.create(
            sink -> {
                try {
                    // Имитация операции, которая может выбросить исключение
                    throw new RuntimeException("Something went wrong");
                } catch (Exception e) {
                    // todo error - передает сигнал ошибки в поток. После вызова этого метода поток завершается с ошибкой.
                    //sink.error(e); // todo если нужно ошибку прокинуть

                    // todo success - возвращаем
                    sink.success(e.getMessage()); // todo если нужно что-то вернуть

                    // todo onCancel - закрываем ресурсы при отписке или cancel
                    sink.onCancel(() -> System.out.println("Cancelled"));

                    // todo onDispose - выполняется при корректном завершении потока или отмене подписки
                    sink.onDispose(() -> System.out.println("Disposed"));


                    sink.onRequest(value -> System.out.println("Request: " + value));

                    // todo contextView - read-only контекст
                    sink.contextView().forEach((object, object2) -> System.out.println("View: " + object + " " + object2));
                }
            }
        );

        mono.subscribe(
            System.out::println,
            error -> System.err.println("Error: " + error.getMessage()),
            () -> System.out.println("Done"),
            Context.of("key1", "value1", "key2", "value2")
        );

        /*
            Something went wrong
            Done
            Disposed
            Request: 9223372036854775807
            View: key1 value1
            View: key2 value2
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
