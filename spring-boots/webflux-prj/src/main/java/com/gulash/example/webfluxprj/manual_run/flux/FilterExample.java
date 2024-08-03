package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.publisher.Flux;

public class FilterExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        filter();
    }

    private static void filter() {
        Flux.range(1, 10)
            .filter(i -> i % 2 == 0) // Оставить только четные числа
            .subscribe(System.out::println);
    }
}

