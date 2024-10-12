package com.gulash.example.assertjdemo.date;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class DataAndTimeTest {

    @Test
    public void testForDateandTime() {
        LocalDateTime localDateTime1 = LocalDateTime.now();
        LocalDateTime localDateTime2 = LocalDateTime.now().minusNanos(1);

        // todo isEqualToIgnoringNanos - игнорирование наносекунд при сравнении
        Assertions.assertThat(localDateTime1)
//            .isEqualTo(localDateTime2); // не пройдёт
            .isEqualToIgnoringNanos(localDateTime1);

        LocalDate date = LocalDate.of(2024, 12, 15);

        Assertions.assertThat(date)
            // todo Проверяем, что дата до ожидаемой
            .isBefore(LocalDate.of(2024, 12, 31))
            // todo Проверяем, что дата после другой даты
            .isAfter(LocalDate.of(2024, 12, 1))

            // todo Проверяем, что дата находится в пределах диапазона
            .isBetween(LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 25))
            //todo Проверяем, что дата строго в пределах диапазона (исключая границы)
            .isStrictlyBetween(LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 25))

            // todo Проверяем, что даты равны
            .isEqualTo("2024-12-15")  // Сравнение строкой

            // todo Проверяем год, месяц и день
            .hasYear(2024)
            .hasMonth(Month.DECEMBER)
            .hasDayOfMonth(15);

    }
}
