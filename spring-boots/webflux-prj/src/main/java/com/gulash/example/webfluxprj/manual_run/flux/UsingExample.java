package com.gulash.example.webfluxprj.manual_run.flux;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
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

        //using();
        usingWhen();
    }

    private static void using() {
        // todo using - аналог try-with-resources
        Flux<String> usingMono = Flux.using(
            // todo resourceSupplier - создание ресурса
            () -> {
                System.out.println("Creating BufferedReader");
                InputStream inputStream = UsingExample.class.getClassLoader().getResourceAsStream("file.txt");

                return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            },

            // todo sourceSupplier - функция, которая использует ресурс для создания Mono.
            bufferedReader -> Flux.create(sink -> {
                    String currentRow = null;
                    try {
                        currentRow = bufferedReader.readLine();

                        while (currentRow != null) {
                            System.out.println("emitted row: %s".formatted(currentRow));
                            sink.next(currentRow);

                            currentRow = bufferedReader.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    sink.complete();
                }
            ),

            // todo resourceCleanup - функция для освобождения ресурса.
            bufferedReader -> {
                System.out.println("resourceCleanup called");
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        );

        Disposable disposable = usingMono
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(value -> System.out.println("subscribe get row: %s".formatted(value)));
            /*
                Creating BufferedReader
                emitted row: 11111
                subscribe get row: 11111
                emitted row: 22222
                subscribe get row: 22222
                emitted row: 33333
                subscribe get row: 33333
                resourceCleanup called
            */

        waitForDisposableEnd(List.of(disposable));
    }

    private static void usingWhen() {
        // todo usingWhen - более гибкий аналог try-with-resources, включая асинхронное освобождение ресурса.
        Flux<String> mono = Flux.usingWhen(
            // todo resourceSupplier - создание ресурса
            Mono.fromCallable(() -> {
                    System.out.println("Creating BufferedReader");
                    InputStream inputStream = UsingExample.class.getClassLoader().getResourceAsStream("file.txt");

                    return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                }
            ),

            // todo sourceSupplier - функция, которая использует ресурс для создания Mono.
            bufferedReader -> Flux.create(sink -> {
                    String currentRow = null;
                    try {
                        currentRow = bufferedReader.readLine();

                        while (currentRow != null) {
                            System.out.println("emitted row: %s".formatted(currentRow));
                            sink.next(currentRow);

                            currentRow = bufferedReader.readLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    sink.complete();
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
            value -> System.out.println("subscribe get row: %s".formatted(value)),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Done")
            );

        waitForDisposableEnd(List.of(disposable));
        /*
            Creating BufferedReader
            emitted row: 11111
            subscribe get row: 11111
            emitted row: 22222
            subscribe get row: 22222
            emitted row: 33333
            subscribe get row: 33333
            resourceCleanup - функция для освобождения ресурса при успешном завершении.
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
