package com.gulash.example.assertjdemo.errors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


public class ThrowByTest {

    @Test
    public void testAssertThatThrownBy() {
        // todo assertThatThrownBy - используется для проверки исключений, которые выбрасываются при выполнении определённого кода.

        // todo satisfies - для проверки любых дополнительных условий
        Assertions.assertThatThrownBy(() -> {
                throw new IllegalArgumentException("Error 42 occurred!");
            }).isInstanceOf(IllegalArgumentException.class)
            .satisfies(exception -> {
                Assertions.assertThat(exception.getMessage()).contains("42");
            });

        // todo Проверка типа исключения
        Assertions.assertThatThrownBy(() -> {
            int result = 10 / 0; // выбрасывает ArithmeticException
        }).isInstanceOf(ArithmeticException.class);

        // todo Проверка сообщения исключения
        Assertions.assertThatThrownBy(() -> {
            throw new IllegalArgumentException("Invalid argument!");
        }).hasMessage("Invalid argument!");

        // todo Проверка части сообщения исключения
        Assertions.assertThatThrownBy(() -> {
            throw new IllegalArgumentException("Invalid argument provided!");
        }).hasMessageContaining("Invalid argument");

        // todo Проверка сообщения и типа исключения одновременно
        Assertions.assertThatThrownBy(() -> {
                throw new IllegalStateException("State error occurred!");
            }).isInstanceOf(IllegalStateException.class)
            .hasMessage("State error occurred!");
    }
}

