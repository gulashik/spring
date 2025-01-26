package com.gulash.example.webfluxprj.manual_run.common;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

public class HooksExample {
    public static void main(String[] args) {
        // todo Hooks.onOperatorDebug - ГЛОБАЛЬНО Включаем отладочные маркеры
        // todo Если в потоке возникнет ошибка, стек вызовов покажет дополнительную информацию, такую как точное место, где создавались операторы.
        //  может значительно замедлить выполнение реактивных цепочек, поскольку сборка отладочной информации требует дополнительных ресурсов.
        Hooks.onOperatorDebug();

        // Создаем поток
        Flux<Integer> flux = Flux.range(1, 5)
            .map(i -> {
                if (i == 3) {
                    throw new RuntimeException("Error at " + i);
                }
                return i * 2;
            });

        // Подписываемся
        flux.subscribe(System.out::println, Throwable::printStackTrace);
    }
}
