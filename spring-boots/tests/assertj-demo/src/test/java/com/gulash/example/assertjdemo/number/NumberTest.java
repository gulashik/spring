package com.gulash.example.assertjdemo.number;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


public class NumberTest {
    @Test
    public void testNumbers() {
        Assertions.assertThat(10).isEqualTo(10);
        Assertions.assertThat(10).isNotEqualTo(20);

        Assertions.assertThat(100).isGreaterThan(10);
        Assertions.assertThat(10).isLessThan(20);
        Assertions.assertThat(100).isGreaterThanOrEqualTo(10);
        Assertions.assertThat(20).isLessThanOrEqualTo(20);

        Assertions.assertThat(1).isPositive();
        Assertions.assertThat(-1).isNegative();
        Assertions.assertThat(0).isZero();
        Assertions.assertThat(10.0).isNotZero();

        Assertions.assertThat(15).isBetween(10, 20); // Включительно
        Assertions.assertThat(10).isStrictlyBetween(1, 11); // Исключительно

        // todo isCloseTo, isNotCloseTo - для проверки чисел, учитывая допустимую разницу
        //  todo within, offset - Ожидаем значение 10.0 с точностью ±0.1
        //  todo byLessThan - проверяет, что значение меньше другого значения на заданную величину
        Assertions.assertThat(10.05)
            .isCloseTo(10.0, AssertionsForClassTypes.within(0.1))
            .isCloseTo(10.0, AssertionsForClassTypes.offset(0.1))
            .isNotCloseTo(100.0, AssertionsForClassTypes.within(0.1));
        Assertions.assertThat(new BigDecimal("10.00"))
            .isCloseTo(new BigDecimal("10.01"), AssertionsForClassTypes.within(new BigDecimal("0.02")))
            .isNotCloseTo(new BigDecimal("100.01"), AssertionsForClassTypes.within(new BigDecimal("0.02")));
        Assertions.assertThat(new BigDecimal("10.00"))
            .isCloseTo(new BigDecimal("9.99"), Assertions.byLessThan(new BigDecimal("0.02")))
            .isNotCloseTo(new BigDecimal("9.9"), Assertions.byLessThan(new BigDecimal("0.02")));

        // todo - isEqualByComparingTo - удобно для BigDecimal
        Assertions.assertThat(new BigDecimal("10.00"))
            .isEqualByComparingTo("10.00");
    }
}
