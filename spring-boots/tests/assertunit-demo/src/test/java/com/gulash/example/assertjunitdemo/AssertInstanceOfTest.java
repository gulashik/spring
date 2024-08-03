package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertInstanceOfTest {
    @Test
    public void test() {
        // todo assertInstanceOf в JUnit 5 используется для проверки того, что объект является экземпляром определенного класса или интерфейса.
        //  Этот метод проверяет, что объект может быть приведен к указанному типу без выброса исключения ClassCastException.
        // шаблон assertInstanceOf(expectedType, actualObject, "Сообщение об ошибке");


        // todo Проверяем, что obj является экземпляром класса String
        Object obj = "Hello, World!";  // Строка, которая является объектом
        Assertions.assertInstanceOf(String.class, obj, () -> "Опционально Supplier or String failed message");

        // todo Проверки с наследованием
        class Vehicle {
            // Общий класс для всех транспортных средств
        }

        class Car extends Vehicle {
            // Класс, представляющий машину
        }

        // Проверяем, что vehicle является экземпляром суперкласса Vehicle
        Vehicle vehicle = new Car();  // Объект класса Car, который является подклассом Vehicle
        Assertions.assertInstanceOf(Vehicle.class, vehicle, () -> "Опционально Supplier or String failed message");
    }
}
