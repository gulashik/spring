package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class FilterExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        filter();
        filterWhen();
    }

    private static void filter() {
        Flux.range(1, 10)
            .filter(i -> i % 2 == 0) // Оставить только четные числа
            .subscribe(System.out::println);
    }

    private static void filterWhen() {
        Flux<String> users = Flux.just("user1", "user2", "user3", "user4", "user5", "user6");

        Flux<String> allowedUsers = users
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

