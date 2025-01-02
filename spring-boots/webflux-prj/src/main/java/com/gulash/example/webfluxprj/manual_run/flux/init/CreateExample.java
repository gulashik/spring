package com.gulash.example.webfluxprj.manual_run.flux.init;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class CreateExample {
    public static void main(String[] args) {
    /*
        todo Flux.create используется для ручного создания реактивного потока, который можно наполнять данными вручную через FluxSink.
            Может быть полезен для интеграции с callback-базированными библиотеками.
            Может быть полезен в случаях, когда поток данных не определен на момент его создания, или требуется низкоуровневый контроль.
    */
        Flux<Integer> integerFlux = Flux.create(
            (FluxSink<Integer> fluxSink) -> {
                // todo next - для эмиссии одного элемента в поток
                fluxSink.next(1);
                fluxSink.next(2);

                // todo error - передает сигнал ошибки в поток. После вызова этого метода поток завершается с ошибкой.
                // fluxSink.error(new RuntimeException("something went wrong"));

                // todo isCancelled - возвращает true, если подписчик отменил подписку. Полезно, чтобы прекратить эмиссию данных
                if (fluxSink.isCancelled()) {
                    fluxSink.complete();
                }
                // todo onCancel - закрываем ресурсы при отписке или cancel
                fluxSink.onCancel(() -> System.out.println("clean resources - onCancel"));

                // todo onDispose - выполняется при корректном завершении потока или отмене подписки
                fluxSink.onDispose(() -> System.out.println("clean resources - onDispose"));

                fluxSink.next(3);
                // todo complete - сигнализирует о завершении потока. После вызова этого метода новые элементы не эмитируются.
                fluxSink.complete(); // нужно завершить
            },
            /*опционально*/
            FluxSink.OverflowStrategy.BUFFER // default Храним пока есть память. Возможно OutOfMemoryError.
            //FluxSink.OverflowStrategy.LATEST // Буфер не используется, и элементы просто передаются в поток, если потребитель готов их принять. Если потребитель не готов, новые элементы игнорируются.
            //FluxSink.OverflowStrategy.DROP // При переполнении новые элементы просто отбрасываются.
            //FluxSink.OverflowStrategy.ERROR // Если буфер переполняется, выбрасывается исключение.
            //FluxSink.OverflowStrategy.IGNORE //Буфер не используется, и элементы просто передаются в поток, если потребитель готов их принять. Если потребитель не готов, новые элементы игнорируются.
        );

        Disposable disposable = integerFlux
            //.take(2) // запрос на отмену
            .subscribe(
                System.out::println,
                Throwable::printStackTrace, // при Exception
                () -> System.out.println("Done")
            );

        //disposable.dispose();
    }
}
