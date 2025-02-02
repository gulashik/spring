package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

public class FilterExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        filter();
        filterWhen();
    }

    private static void filter() {
        Mono.just(1)
            .filter(i -> i % 2 == 0) // Оставить только четные числа
            .switchIfEmpty(Mono.just(0)) // если не чётное, то fallback
            .subscribe(System.out::println);
    }

    private static void filterWhen() {
        Mono<String> users = Mono.just("user1");

        Mono<String> allowedUsers = users
            // todo filterWhen - как фильтр только с Асинхронной операции (вызова внешнего API, запроса к базе данных и т.д.)
            .filterWhen(
                currUser -> {
                    // todo возвращаем Publisher
                    return Mono.fromSupplier(() -> {
                            if ("user1".equals(currUser)) {
                                return true;
                            } else if ("user3".equals(currUser)) {
                                return false;
                            }
                            return Math.random() > 0.5; // Случайное решение для других пользователей
                        }
                    );
                }
            );

        Disposable disposable = allowedUsers.subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));

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

