package com.gulash.example.webfluxprj.manual_run.flux.block;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class SortExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        sort();
    }

    private static void sort() {
        // todo sort - сортировки элементов потока. ВНАЧАЛЕ ВСЁ ПОЛУЧАЕМ В ПАМЯТЬ.

        Comparator<Integer> comparatorManual = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        };
        Flux.range(1, 5)
            .sort(comparatorManual) // todo разные компараторы
            //.sort(Comparator.reverseOrder())
            .doFinally(s -> System.out.println("done\n"))
            .subscribe(System.out::println);

        Comparator<Integer> comparing = Comparator.comparing(integer -> integer % 2);
        Comparator<Integer> thenComparing = comparing.thenComparing(Comparator.reverseOrder());

        Flux.range(1, 5)
            .sort(thenComparing) // todo разные компараторы
            .doFinally(s -> System.out.println("done\n"))
            .subscribe(System.out::println);
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
