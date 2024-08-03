package com.gulash.example.assertjdemo.objectcompare;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class UsingComparatorTest {
    @Test
    public void testUsingComparator() {

        // todo Создаём свой компаратор
        Comparator<LocalDateTime> localDateTimeComparator = new Comparator<>() {
            private void print(LocalDateTime o1, LocalDateTime o2) {
                System.out.printf("was %s now %s%n", o1, o2);
            }
            @Override
            public int compare(LocalDateTime o1, LocalDateTime o2) {
                LocalDateTime localDateTime1 = o1.truncatedTo(ChronoUnit.MINUTES);
                LocalDateTime localDateTime2 = o2.truncatedTo(ChronoUnit.MINUTES);

                print(o1, localDateTime1);
                print(o2, localDateTime2);

                return localDateTime1.compareTo(localDateTime2);
            }
        };

        LocalDateTime actual = LocalDateTime.now();
        LocalDateTime expected = actual.plusNanos(100);

        Assertions.assertThat(actual)
            // todo используем компаратор
            .usingComparator(localDateTimeComparator)
            .isEqualTo(expected);

        // todo пример для ЧАСТИЧНОЕ СРАВНЕНИЕ используем thenComparing
        record User(String firstName, String lastName, LocalDateTime birthday, String email) {}
        // todo частичное сравнение
        Comparator<User> userComparator = Comparator.comparing(User::firstName).thenComparing(User::lastName);

        User userActual = new User("Fname","Lname",LocalDateTime.now(),"email1@gmail.com1");
        User userExpected = new User("Fname","Lname",LocalDateTime.now().minusHours(1),"email1@gmail.com2");

        Assertions.assertThat(userActual)
            .as("ЧАСТИЧНОЕ СРАВНЕНИЕ используем thenComparing с полями firstName и lastName")
            .usingComparator(userComparator)
            .isEqualTo(userExpected);
    }
}
