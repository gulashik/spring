package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssertThrowTest {
    @Test
    public void test() {
        class someClass{
            public Integer div(int a, int b){
                return a / b;
            }
        }

        // todo assertThrows
        Assertions.assertThrows(
            ArithmeticException.class, /*класс ошибки*/
            () -> new someClass().div(1,0), /*что проверяем нужен вызов объекта*/
            () -> "Опционально Supplier or String for fail message" /**/
        );

        // todo assertDoesNotThrow
        Assertions.assertDoesNotThrow(
            () -> new someClass().div(1,10), /*что проверяем нужен вызов объекта*/
            () -> "Опционально Supplier or String for fail message" /**/
        );
        Assertions.assertDoesNotThrow(() -> {
            Thread thread = new Thread(() -> System.out.println("Работает поток"));
            thread.start();
            //int i = 1 / 0;
            thread.join();
        }, "Поток должен завершиться без исключений");
    }
}
