package com.gulash.example.webfluxprj.manual_run.flux.init.publish_subscribe_swtich;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class SubscribeExample {
    public static void main(String[] args) {
        // todo subscribe() - для cold publisher инициирует выполнение реактивного потока, который до этого определяет только его поведение (ленивое исполнение).
        // todo В Spring WebFlux редко используется subscribe() напрямую. Вместо этого данные возвращаются контроллером.
        //   Spring сам выполняет подписку для передачи данных клиенту.
        Flux.range(1, 10)
            // todo шаг где используется контекст
            .flatMap( currValue -> {
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
    }
}

