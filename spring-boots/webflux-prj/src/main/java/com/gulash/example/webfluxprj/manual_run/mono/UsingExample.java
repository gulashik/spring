package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UsingExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        using();
        usingWhen();
    }

    private static void using() {
        // todo using - аналог try-with-resources
        Mono<String> usingMono = Mono.using(
            // todo resourceSupplier - создание ресурса
            () -> {
                InputStream inputStream = UsingExample.class.getClassLoader().getResourceAsStream("file.txt");

                return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            },

            // todo sourceSupplier - функция, которая использует ресурс для создания Mono.
            bufferedReader -> Mono.fromCallable(() -> {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        throw new RuntimeException("File is empty");
                    }
                    return line;
                }
            ),

            // todo resourceCleanup - функция для освобождения ресурса.
            bufferedReader -> {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );

        Disposable disposable = usingMono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(System.out::println);
        // 11111

        waitForDisposableEnd(List.of(disposable));
    }

    private static void usingWhen() {
        // todo usingWhen - более гибкий аналог try-with-resources, включая асинхронное освобождение ресурса.
        Mono<String> mono = Mono.usingWhen(
            // todo resourceSupplier - создание ресурса
            Mono.fromCallable(() -> {
                    InputStream inputStream = UsingExample.class.getClassLoader().getResourceAsStream("file.txt");

                    return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                }
            ),

            // todo sourceSupplier - функция, которая использует ресурс для создания Mono.
            bufferedReader -> Mono.fromCallable(() -> {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        throw new RuntimeException("File is empty");
                    }
                    return line;
                }
            ),

            // todo resourceCleanup - функция для освобождения ресурса при успешном завершении.
            bufferedReader -> Mono.fromRunnable(() -> {
                    try {
                        System.out.println("resourceCleanup - функция для освобождения ресурса при успешном завершении.");
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            ),

            // todo asyncError - функция для асинхронного освобождения ресурса при ошибке.
            (bufferedReader, error) -> Mono.fromRunnable(() -> {
                    try {
                        System.out.println("asyncError - функция для асинхронного освобождения ресурса при ошибке.");
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            ),

            // todo asyncCancel - функция для асинхронного освобождения ресурса при отмене.
            reader -> Mono.fromRunnable(() -> {
                    try {
                        System.out.println("asyncCancel - функция для асинхронного освобождения ресурса при отмене.");
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            )
        );

        Disposable disposable = mono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                System.out::println,
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Done")
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            resourceCleanup - функция для освобождения ресурса при успешном завершении.
            11111
            Done
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
