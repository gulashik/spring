package com.gulash.example.webfluxprj.manual_run.flux.init;

import reactor.core.publisher.Flux;

public class JustExample {
    public static void main(String[] args) {
        // just(value) — создает поток, содержащий ноль и одно значение.
        Flux.just("one","two","three")
            .subscribe(System.out::println);
        /*
            one
            two
            three
        */
    }
}
