package com.gulash.example.assertjdemo.errors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeTest {

    @Test
    public void test() {
        // todo assertThatCode - проверки поведения кода, который может генерировать исключения.
        Assertions.assertThatCode(() -> {
                int result = 10 / 2; // код не выбрасывает исключение
            }
        ).doesNotThrowAnyException(); // нет исключений

        Assertions.assertThatCode(() -> {
                int result = 10 / 0; // выбросит ArithmeticException
            }
        ).isInstanceOf(ArithmeticException.class); // определённый тип исключений

        Assertions.assertThatCode(() -> {
            throw new IllegalArgumentException("Invalid argument!");
        }).hasMessage("Invalid argument!"); // сообщение из исключения
        Assertions.assertThatCode(() -> {
            throw new IllegalArgumentException("Invalid argument provided!");
        }).hasMessageContaining("Invalid argument");
    }
}
