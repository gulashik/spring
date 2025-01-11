package com.gulash.example.webfluxprj.manual_run.flux.flux_to_mono;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class HasExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md
        hasElementsLikeNotEmpty();
        hasElementLikeContains();
    }

    private static void hasElementsLikeNotEmpty() {
        Flux<String> fluxString = Flux.just("a", "b", "c");
        // todo hasElements - Mono<Boolean>, который содержит результат проверки содержит поток хоть один элемент.
        Mono<Boolean> hasElementsMonoEmpty = fluxString.hasElements();

        hasElementsMonoEmpty.subscribe(result -> System.out.println("Has elements: " + result));
    }
    private static void hasElementLikeContains() {
        Flux<Integer> fluxNumb = Flux.just(1, 2, 3, 4);

        // todo hasElement -  Mono<Boolean>, который содержит результат проверки.
        Mono<Boolean> hasElementMono = fluxNumb.hasElement(3);

        hasElementMono.subscribe(result -> System.out.println("Has element 3: " + result));
    }

    // ожидалка окончания Disposable
    private static void waitForDisposableEnd(List<Disposable> disposableList) {
        disposableList.forEach(
            // isDisposed
            //  true, если ресурс был освобожден (закрыт или отменен).
            //  false, если ресурс все еще активен.
            disposable -> { while (!disposable.isDisposed()) {}}
        );
    }
}
