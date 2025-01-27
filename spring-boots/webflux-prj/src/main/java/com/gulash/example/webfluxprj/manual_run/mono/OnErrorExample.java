package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class OnErrorExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        // todo при вызове  generateError() не сработают методы
        //onErrorComplete();
        //onErrorMap();
        //onErrorResume();
        onErrorReturn();
    }

    private static void onErrorComplete() {
        Mono<Integer> result = Mono
            //.just(generateError()) // todo не сработает
            .<Integer>error(new RuntimeException("Ошибка"))
            // todo onErrorComplete - для завершения потока в случае ошибки
            .onErrorComplete()  // В случае ошибки возвращается пустой Mono
            .doOnTerminate(() -> System.out.println("doOnTerminate"));

        result.subscribe(
            value -> System.out.println("Получено значение: " + value)
            ,error -> System.out.println("Ошибка: " + error)  // Не будет вызвано
            ,() -> System.out.println("doOnComplete")
        );
    }

    private static void onErrorMap() {
        Mono<Integer> result = Mono
            //.just(generateError()) // todo не сработает
            .<Integer>error(new RuntimeException("Some exception"))
            // // todo onErrorMap - преобразование в другую ошибку.
            //      Это полезно, если нужно изменить тип ошибки на более специфичный или более удобный для обработки.
            .onErrorMap(ex -> new IllegalStateException("Новая ошибка: " + ex.getMessage()));

        result.subscribe(
            value -> System.out.println("Получено значение: " + value),
            error -> System.out.println("Ошибка: " + error.getMessage())  // Будет выведено "Ошибка: Новая ошибка: Some exception"
        );
    }

    private static void onErrorResume() {
        Mono<Integer> result = Mono
            //.just(generateError()) // todo не сработает
            .<Integer>error(new RuntimeException("Ошибка"))
            // todo onErrorResume - заменить ошибку на другой Mono
            .onErrorResume(ex -> Mono.just(42));  // Заменим ошибку значением 42

        result.subscribe(
            value -> System.out.println("Получено значение: " + value),
            error -> System.out.println("Ошибка: " + error)  // Ошибка не будет выведена
        );
    }
    private static void onErrorReturn() {
        Mono<Integer> result = Mono
            //.just(generateError()) // todo не сработает
            .<Integer>error(new RuntimeException("Ошибка"))
            // todo onErrorReturn - возвращает фиксированное значение в случае ошибки.
            .onErrorReturn(24);  // При ошибке возвращается значение 42

        result.subscribe(
            value -> System.out.println("Получено значение: " + value),
            error -> System.out.println("Ошибка: " + error)  // Ошибка не будет выведена
        );

    }

    private static Integer generateError() {
        throw new RuntimeException("Some exception");
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
