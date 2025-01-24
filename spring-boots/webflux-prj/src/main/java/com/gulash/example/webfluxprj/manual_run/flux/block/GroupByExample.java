package com.gulash.example.webfluxprj.manual_run.flux.block;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class GroupByExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        groupBy();
    }

    private static void groupBy() {


        // Получаем Flux<GroupedFlux<Character, String>>
        Flux<GroupedFlux<Character, String>> groupedFlux =
            Flux.just("apple", "banana", "apricot", "blueberry", "cherry")
                // todo groupBy - позволяет разделить исходный поток на несколько потоков, каждый из которых обрабатывает элементы, принадлежащие определенной группе.
                //  todo GroupedFlux<K, V> - КАЖДАЯ ГРУППА обрабатывается как ОТДЕЛЬНЫЙ ПОТОК.
                .groupBy(
                    // todo keyMapper - ключ группы
                    word -> word.charAt(0),
                    // todo valueMapper - если нужно предварительная обработка перед добавлением в группу
                    s -> s.toUpperCase()
                );

        Flux<String> stringFlux = groupedFlux.flatMap(
            (GroupedFlux<Character, String> currGroup) -> {
                // todo GroupedFlux<K, V> extends Flux<V> + метод key
                Character key = currGroup.key();

                // Логично во что-то собрать элементы группы
                Mono<List<String>> listMono = currGroup.collectList();

                // todo Каждую группу во что-то нужное
                return listMono.map(
                    (List<String> strings) -> "Grouped Flux key: " + key + " " + strings
                );
            }
        );

        Disposable disposable = stringFlux
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);

        waitForDisposableEnd(List.of(disposable));
        /*
            Grouped Flux key: a [APPLE, APRICOT]
            Grouped Flux key: b [BANANA, BLUEBERRY]
            Grouped Flux key: c [CHERRY]
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
