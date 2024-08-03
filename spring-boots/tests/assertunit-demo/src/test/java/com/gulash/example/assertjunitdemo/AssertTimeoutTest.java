package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class AssertTimeoutTest {
    @Test
    void test() {
        /*
        *   assertTimeout - Ожидает завершения блока кода даже в случае превышения времени.
        *   assertTimeoutPreemptively - Прерывает выполнение блока кода, если превышен лимит времени, что может быть небезопасно.
        * */
        // todo assertTimeout проверяет, что выполнение блока кода укладывается в указанный лимит времени.
        //  Если код не укладывается в заданный интервал, тест завершается с ошибкой, но блок кода завершается до конца (не прерывается).

        Assertions.assertTimeout(
            Duration.ofSeconds(1), // timeout
            () -> {
                Thread.sleep(100); // todo блок кода завершается до конца (НЕ ПРЕРЫВАЕТСЯ)
                System.out.println("Hello World");
            },
            () -> "Опционально Supplier or String failed message"
        );

        // todo assertTimeoutPreemptively - прерывает выполнение блока кода, если он превышает заданный лимит времени.
        //  Это полезно для предотвращения долгих операций, но следует быть осторожным,
        //      так как прерывание может вызывать побочные эффекты (например, оставлять незакрытые ресурсы).
        Assertions.assertTimeoutPreemptively(
            Duration.ofSeconds(1), // timeout
            () -> {
                Thread.sleep(30/*00*/); // todo блок кода ПРЕРЫВАЕТСЯ
                System.out.println("Hello World");
            },
            () -> "Опционально Supplier or String failed message"
        );
    }
}
