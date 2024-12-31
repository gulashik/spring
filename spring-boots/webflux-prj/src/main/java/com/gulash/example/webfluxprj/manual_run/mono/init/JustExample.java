package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.publisher.Mono;

public class JustExample {
    public static void main(String[] args) {
        // Mono.just(value) — создает поток, содержащий ноль и одно значение.
        Mono.just("one")
            .subscribe(System.out::println);
    }
}
