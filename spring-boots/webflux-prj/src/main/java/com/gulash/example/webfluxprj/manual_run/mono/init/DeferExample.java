package com.gulash.example.webfluxprj.manual_run.mono.init;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class DeferExample {
    public static void main(String[] args) {
/*
        ****Initial time for EAGER: 2025-01-02T17:17:28.157247

        Время уже больше чем в EAGER. Т.к. eager уже создан: 2025-01-02T17:17:28.233393

        Subscribing to eager Mono
        Всегда ОДИНАКОВОЕ время: 2025-01-02T17:17:28.157247

        Subscribing to deferred Mono
        ****Initial time for DEFER: 2025-01-02T17:17:29.353923
        Всегда РАЗНОЕ время: 2025-01-02T17:17:29.353923
        ****Initial time for DEFER: 2025-01-02T17:17:30.471517
        Всегда РАЗНОЕ время: 2025-01-02T17:17:30.471517

        Subscribing to eager Mono
        Всегда ОДИНАКОВОЕ время: 2025-01-02T17:17:28.157247
*/
        // todo Mono.just - значения формируются СРАЗУ в МОМЕНТ СОЗДАНИЯ Mono экземпляра.
        Mono<LocalDateTime> eager = Mono.just(getNow("EAGER"));

        // todo Mono.defer - значения формируются ПРИ КАЖДОЙ ПОДПИСКЕ на Mono экземпляр.
        //  Ленивая загрузка: Используется, чтобы избежать выполнения операций заранее,
        //  особенно если операция зависит от текущего контекста или состояния.
        // Удобно для БД т.к. реальное обращение будет при каждой подписке.
        //  например Mono<User> userMono = Mono.defer(() -> userRepository.findById(userId));
        Mono<LocalDateTime> deferred = Mono.defer(() -> Mono.just(getNow("DEFER")));

        System.out.println("Время уже больше чем в EAGER. Т.к. eager уже создан: " + LocalDateTime.now());

        System.out.println("Subscribing to eager Mono");
        eager.subscribe(
            localDateTime -> System.out.println("Всегда ОДИНАКОВОЕ время: " + localDateTime)
        );

        sleep();
        System.out.println("Subscribing to deferred Mono");
        deferred.subscribe(
            localDateTime -> System.out.println("Всегда РАЗНОЕ время: " + localDateTime)
        );
        sleep();
        deferred.subscribe(
            localDateTime -> System.out.println("Всегда РАЗНОЕ время: " + localDateTime)
        );

        sleep();
        System.out.println("Subscribing to eager Mono");
        eager.subscribe(
            localDateTime -> System.out.println("Всегда ОДИНАКОВОЕ время: " + localDateTime)
        );
    }

    private static LocalDateTime getNow(String type) {
        var now = LocalDateTime.now();
        System.out.println("****Initial time for %1$s: %2$s".formatted(type, now));
        return now;
    }
    private static void sleep() {
        try { Thread.sleep(1111); } catch (InterruptedException ignored) {}
    }
}
