package com.gulash.example.webfluxprj.manual_run.flux.init.hot;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class SinkExample {
    public static void main(String[] args) {
        // todo Создаем Sink с типом multicast
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        // todo Получаем горячий Flux
        Flux<String> hotFlux =
            sink
                .asFlux()
                // можно использовать кэширование если нужно
                //.cache()
                /*.cache(
                    100, // сколько в кэше
                    Duration.ofHours(1), // храним только
                    Schedulers.parallel() // на каком планировщике будет работать механизм кэширования.
                )*/
            ;

        // todo Подписчик 1
        hotFlux.subscribe(data -> System.out.println("Подписчик 1 получил: " + data));

        // todo Эмитим данные
        sink.tryEmitNext("Привет");
        sink.tryEmitNext("Мир");

        // todo Подписчик 2
        hotFlux.subscribe(data -> System.out.println("Подписчик 2 получил: " + data));

        // todo Эмитим дополнительные данные
        sink.tryEmitNext("WebFlux");

        // завершение посмотреть
        //sink.tryEmitComplete();
        //sink.emitComplete(xxx);
    }
}
