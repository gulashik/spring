package com.gulash.example.webfluxprj.manual_run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.logging.Level;

public class LogExample {
        // Используем SLF4J для обычного логирования
        private static final Logger logger = LoggerFactory.getLogger(LogExample.class);

    public static void main(String[] args) {

        // todo  - позволяет отлаживать реактивный поток, добавляя события (onSubscribe, onNext, onError, onComplete, onCancel) в журнал.
        /*
        Выбор уровня
        •	Продакшн: INFO или WARNING для контроля состояния системы и обработки ошибок.
        •	Отладка: FINE или FINER для изучения работы потоков.
        •	Диагностика ошибок: SEVERE или ERROR для критических исключений.
        */


        Flux.just("data1", "data2", "data3")
            // Используем java.util.logging.Level.FINE (эквивалент DEBUG в SLF4J)
            .log("SourceFlow", Level.FINE)
            .log()
            .map(data -> {
                // Для обычного логирования используем SLF4J logger
                logger.debug("Processing data: {}", data);
                return data.toUpperCase();
            })
            // Используем Level.INFO для информационного уровня
            .log("TransformedFlow", Level.INFO)
            .filter(data -> data.length() > 4)
            // Используем Level.WARNING (эквивалент WARN в SLF4J)
            .log("FilteredFlow", Level.WARNING)

            .subscribe(System.out::println);
    }
}
