package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DistinctExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }
    record User(Integer id, String name) {}

    private static void example() {
        // Кеширование в памяти - Для определения уникальности элементы кэшируются в памяти.
        //  Это значит, что большое количество уникальных данных может потребовать значительных ресурсов.
        Flux.just(1,2,3,4,1,2,3)
            // todo distinct() - фильтруются дубликаты
            // todo используется equals и hashCode
            .distinct()
            .subscribe(System.out::println);

        Flux<User> users = Flux.just(
            new User(2, "Bob"),
            new User(1, "Alice"),
            new User(1, "Alice"),
            new User(3, "Charlie"),
            new User(2, "Bob")
        );

        Disposable disposable = users
            // todo distinct(keySelector) - фильтруются дубликаты
            .distinct(
                // todo keySelector - на основе чего сравниваем
                user -> user.id,
                // todo distinctCollectionSupplier - используем HashSet для хранения уникальных ключей
                /*todo Опционально*/ ConcurrentHashMap<Integer, Boolean>::new,
                // todo distinctPredicate - Уникальность определяется добавлением в HashSet
                /*todo Опционально*/ (map, key) -> map.putIfAbsent(key, true) == null,
                // todo cleanup - Очистка коллекции после завершения потока
                /*todo Опционально*/ (ConcurrentHashMap<Integer, Boolean> concurrentHashMap) -> concurrentHashMap.clear()
                )
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            1
            2
            3
            4
            User[id=2, name=Bob]
            User[id=1, name=Alice]
            User[id=3, name=Charlie]
        */
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