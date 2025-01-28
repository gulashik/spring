package com.gulash.example.webfluxprj.manual_run.mono;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;

public class SubscribeExample {
    public static void main(String[] args) {
        // todo предварительно запуск spring-boots/webflux-prj/compose.md

        example();
    }

    private static void example() {
        // todo В Spring WebFlux редко используется subscribe() напрямую. Вместо этого данные возвращаются контроллером.
        //   Spring сам выполняет подписку для передачи данных клиенту.
        Disposable disposable = Mono.just(1)
            // todo шаг где используется контекст
            .flatMap(currValue -> {
                    // todo достаём из контекста и используем
                    Mono<String> stringMono = Mono.deferContextual(contextView -> {
                            String val = contextView.getOrDefault("user", "def_user");
                            // что-то нужное
                            return Mono.just(val);
                        }
                    );
                    return stringMono.map(ctxValue -> " from context value '%s'; current value '%s'".formatted(ctxValue, currValue));
                }
            )
            // todo промежуточные шаги
            .doOnNext(s -> System.out.printf("doOnNext step current value is: %s%n", s))
            .subscribe(
                // todo Consumer с нужным действием
                currentValue -> System.out.println("Нужные действия c элементом потока %s".formatted(currentValue)),
                // todo optional Consumer с обработкой Exception-а
                throwable -> throwable.printStackTrace(),
                // todo optional Завершающее действие
                () -> System.out.println("Done"),
                // todo optional Начальный контекст(кладём что нужно в контекст, пароли, токены и т.д.)
                Context.of("user", "Иван")
            );

        waitForDisposableEnd(List.of(disposable));
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
