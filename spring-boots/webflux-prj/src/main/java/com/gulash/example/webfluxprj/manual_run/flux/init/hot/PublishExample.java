package com.gulash.example.webfluxprj.manual_run.flux.init.hot;

import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

public class PublishExample {
    public static void main(String[] args) throws InterruptedException {
        /*
        Использование в WebFlux:
	        Подходит для потоковой передачи данных (например, SSE или WebSocket).
	        Эффективно для обработки событий реального времени (например, данные с сенсоров или уведомления).
        */

//        examplePublishAndConnect();
//        examplePublishAndAutoconnect();
//        examplePublishAndRefCount();
        exampleShare();
    }

    private static void exampleShare() throws InterruptedException {
        // todo .publish() - делает поток "горячим"(Преобразует поток из "холодного" (`Cold Publisher`) в "горячий" (`Hot Publisher`))
        //			+ refCount(x) -  активирует поток при X подписчиков (в отличие от ``).
        //              refCount(0) - немедленно активирует поток, даже если нет подписчиков и заканчивается при полной отписке
        //              refCount(1) -  активирует поток при появлении одного подписчика и заканчивается при полной отписке

        // todo refCount vs autoConnect отличия
        //  .autoConnect(2) - эмиссия начинается при наличии двух подписчиков и НЕ заканчивается при полной отписке.
        //  .refCount(2) - эмиссия начинается при наличии двух подписчиков и ЗАКАНЧИВАЕТСЯ при полной отписке
        //  .share() - аналог publish().refCount() Эмиссия начинается при наличии ОДНОГО подписчика и и ЗАКАНЧИВАЕТСЯ при полной отписке.

        Flux<Integer> refCountFlux = Flux.range(1, 15)
            .delayElements(Duration.ofMillis(500), Schedulers.parallel())
            .doOnCancel(() -> System.out.println("Flux canceled"))
            .doFinally(signalType -> System.out.println("Flux finally by type-" + signalType))
            .doOnNext(System.out::println)
            .share() // аналог publish().refCount() Эмиссия начинается при наличии ОДНОГО подписчика и и ЗАКАНЧИВАЕТСЯ при полной отписке.
            //.publish().autoConnect() Всё будет прододжаться при отписке
            ;

        Disposable sub1 = refCountFlux.subscribe(data -> System.out.println("Subscriber 1: " + data));

        Thread.sleep(1000);

        Disposable sub2 = refCountFlux.subscribe(data -> System.out.println("Subscriber 2: " + data));

        Thread.sleep(2000);

        // Завершение подписки. При refCount публишер будет остановлен.
        sub1.dispose();
        sub2.dispose();

        Thread.sleep(5000);
    }

    private static void examplePublishAndRefCount() throws InterruptedException {
        // todo .publish() - делает поток "горячим"(Преобразует поток из "холодного" (`Cold Publisher`) в "горячий" (`Hot Publisher`))
        //			+ refCount(x) -  активирует поток при X подписчиков (в отличие от ``).
        //              refCount(0) - немедленно активирует поток, даже если нет подписчиков и заканчивается при полной отписке
        //              refCount(1) -  активирует поток при появлении одного подписчика и заканчивается при полной отписке

        // todo refCount vs autoConnect отличия
        //  .autoConnect(2) - эмиссия начинается при наличии двух подписчиков и НЕ заканчивается при полной отписке.
        //  .refCount(2) - эмиссия начинается при наличии двух подписчиков и ЗАКАНЧИВАЕТСЯ при полной отписке
        //  .share() - аналог publish().refCount() Эмиссия начинается при наличии ОДНОГО подписчика и и ЗАКАНЧИВАЕТСЯ при полной отписке.

        Flux<Integer> refCountFlux = Flux.range(1, 5)
            .delayElements(Duration.ofMillis(500), Schedulers.parallel())
            .doOnNext(System.out::println)
            .publish()
            //.autoConnect(2) // Эмиссия начинается при наличии двух подписчиков и НЕ заканчивается при полной отписке.
            .refCount(2) // Эмиссия начинается при наличии двух подписчиков и ЗАКАНЧИВАЕТСЯ при полной отписке
            ;

        Disposable sub1 = refCountFlux.subscribe(data -> System.out.println("Subscriber 1: " + data));

        Thread.sleep(1000);

        Disposable sub2 = refCountFlux.subscribe(data -> System.out.println("Subscriber 2: " + data));

        Thread.sleep(2000);

        // Завершение подписки. При refCount публишер будет остановлен.
        sub1.dispose();
        sub2.dispose();

        Thread.sleep(5000);
    }

    private static void examplePublishAndAutoconnect() throws InterruptedException {
        // todo .publish() - делает поток "горячим"(Преобразует поток из "холодного" (`Cold Publisher`) в "горячий" (`Hot Publisher`))
        //			+ autoConnect(x) -  активирует поток при X подписчиков (в отличие от ``).
        //              autoConnect(0) - немедленно активирует поток, даже если нет подписчиков
        //              autoConnect(1) -  активирует поток при появлении одного подписчика

        // todo refCount vs autoConnect отличия
        //  .autoConnect(2) - эмиссия начинается при наличии двух подписчиков и НЕ заканчивается при полной отписке.
        //  .refCount(2) - эмиссия начинается при наличии двух подписчиков и ЗАКАНЧИВАЕТСЯ при полной отписке
        //  .share() - аналог publish().refCount() Эмиссия начинается при наличии ОДНОГО подписчика и и ЗАКАНЧИВАЕТСЯ при полной отписке.

        // Создаем холодный Flux
        Flux<String> coldFlux = Flux
            .interval(Duration.ofSeconds(1))
            .map(i -> "Событие: " + i)
            .take(10)
            ;

        Flux<String> hotFlux =
            coldFlux
                .publish() // todo Преобразуем в горячий Flux
                .autoConnect(1) // todo активирует поток при появлении одного подписчика
            ;

        // todo Подписчик 1 подписался и стразу стартутет поток
        Disposable disposable1 = hotFlux.subscribe(data -> System.out.println("Подписчик 1 получил: " + data));

        Thread.sleep(3000); // Ждем 3 секунды

        // todo Подписчик 2 подключается позже и получает меньше событий
        Disposable disposable2 = hotFlux.subscribe(data -> System.out.println("Подписчик 2 получил: " + data));

        waitForDisposableEnd(List.of(disposable1,disposable2)); // Ждем, чтобы посмотреть результаты
    }

    private static void examplePublishAndConnect() throws InterruptedException {
        // todo .publish() - делает поток "горячим"(Преобразует поток из "холодного" (`Cold Publisher`) в "горячий" (`Hot Publisher`))
        //			+ connect() - немедленный запуск потока
        // Создаем холодный Flux
        Flux<String> coldFlux = Flux
            .interval(Duration.ofSeconds(1))
            .map(i -> "Событие: " + i)
            .take(10)
            ;

        // todo Преобразуем в горячий Flux
        ConnectableFlux<String> hotFlux = coldFlux.publish();

        // todo Подписчик 1 подписался раньше чем начали эмитить
        Disposable disposable1 = hotFlux.subscribe(data -> System.out.println("Подписчик 1 получил: " + data));

        // todo Стартуем генерацию
        hotFlux.connect();

        Thread.sleep(3000); // Ждем 3 секунды

        // todo Подписчик 2 подключается позже и получает меньше событий
        Disposable disposable2 = hotFlux.subscribe(data -> System.out.println("Подписчик 2 получил: " + data));

        waitForDisposableEnd(List.of(disposable1,disposable2)); // Ждем, чтобы посмотреть результаты
    }

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
