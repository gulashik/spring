package com.gulash.example.webfluxprj.manual_run.flux.init.cold;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public class DeferExample {
    public static void main(String[] args) {
/*
        ****Initial time for EAGER: 2025-01-02T17:07:45.087232

        Время уже больше чем в EAGER. Т.к. eager уже создан: 2025-01-02T17:07:45.173534

        Subscribing to eager Flux
        Всегда ОДИНАКОВОЕ время: 2025-01-02T17:07:45.087232

        Subscribing to deferred Flux
        ****Initial time for DEFER: 2025-01-02T17:07:46.293533
        Всегда РАЗНОЕ время: 2025-01-02T17:07:46.293533
        ****Initial time for DEFER: 2025-01-02T17:07:47.412046
        Всегда РАЗНОЕ время: 2025-01-02T17:07:47.412046

        Subscribing to eager Flux
        Всегда ОДИНАКОВОЕ время: 2025-01-02T17:07:45.087232
*/
        // todo Flux.just - значения формируются СРАЗУ в МОМЕНТ СОЗДАНИЯ Flux экземпляра.
        Flux<LocalDateTime> eager = Flux.just(getNow("EAGER"));

        // todo Flux.defer - значения формируются ПРИ КАЖДОЙ ПОДПИСКЕ на Flux экземпляр.
        //  Ленивая загрузка: Используется, чтобы избежать выполнения операций заранее,
        //  особенно если операция зависит от текущего контекста или состояния.
        // Удобно для БД т.к. реальное обращение будет при каждой подписке.
        //  например Mono<User> userMono = Mono.defer(() -> userRepository.findById(userId));
        Flux<LocalDateTime> deferred = Flux.defer(() -> Flux.just(getNow("DEFER")));

        System.out.println("Время уже больше чем в EAGER. Т.к. eager уже создан: " + LocalDateTime.now());

        System.out.println("Subscribing to eager Flux");
        eager.subscribe(
            localDateTime -> System.out.println("Всегда ОДИНАКОВОЕ время: " + localDateTime)
        );

        sleep();
        System.out.println("Subscribing to deferred Flux");
        deferred.subscribe(
            localDateTime -> System.out.println("Всегда РАЗНОЕ время: " + localDateTime)
        );
        sleep();
        deferred.subscribe(
            localDateTime -> System.out.println("Всегда РАЗНОЕ время: " + localDateTime)
        );

        sleep();
        System.out.println("Subscribing to eager Flux");
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
