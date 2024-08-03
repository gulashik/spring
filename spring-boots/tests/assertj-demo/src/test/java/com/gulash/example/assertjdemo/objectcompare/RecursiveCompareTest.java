package com.gulash.example.assertjdemo.objectcompare;

import com.gulash.example.assertjdemo.entity.Book;
import com.gulash.example.assertjdemo.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;


class RecursiveCompareTest {

    @Test
    void testSaveAndFind() {
        Book bookActual = new Book(1L, "my title", new User(1L,"author1"),"12345", /*attr1*/null, 2);
        Book bookActual1 = new Book(2L, "my title", new User(1L,"author1"),"12345", /*attr1*/1, 2);

        Book bookExpected = new Book(10L, "my TITLE", new User(1L,"author1"),"54321", 1, null);
        Book bookExpected1 = new Book(10L, "my TITLE", new User(1L,"author1"),"54321", 1, 2);

        // todo Comparator должен вернуть НОЛЬ если всё OK
        Comparator<String> strComparator = (String str1, String str2) -> str1.length() == str2.length() ? 0/*НОЛЬ = OK*/ : 1;

        assertThat(bookActual)
            // todo Метод рекурсивно обходит поля объектов и сравнивает их значения.=========
            .usingRecursiveComparison()

            // todo Настройки для сравнения=============
            // todo игнорируем поле по имени поля
            .ignoringFields("id")
            .ignoringFieldsMatchingRegexes(".?d","i.?")
            // todo игнорируем поля с null с указанной стороны
            .ignoringActualNullFields() // со стороны Actual
            .ignoringExpectedNullFields() // со стороны Expected

            // todo используем штатный компаратор для ОПРЕДЕЛЁННОГО ТИПА
            .withComparatorForType(String.CASE_INSENSITIVE_ORDER, String.class)
            // todo используем свой компаратор для ОПРЕДЕЛЁННОГО ТИПА
            .withComparatorForType(
                // String::compareToIgnoreCase
                // или
                (str1, str2) -> {
                    System.out.println(str1 + " - " + str2);
                    return str1.compareToIgnoreCase(str2);
                },
                String.class // todo используем только для типа String
            )
            // todo используем свой компаратор ДЛЯ НУЖНЫХ ПОЛЕЙ
            .withComparatorForFields(strComparator, "isbn","isbn2")

            // todo Сравниваем с учётом настроек===========
            .isEqualTo(bookExpected);
    }

    @Test
    void testSaveAndFind2() {
        Book bookActual = new Book(1L, "my title", new User(1L,"author1"),"12345", 1, 2);

        Book bookExpected = new Book(10L, "my title", new User(1L,"author1"),"54321", 1, 2);

        assertThat(bookActual)
            .usingRecursiveComparison()

            // todo только поля определённые поля
            .comparingOnlyFields("author","title")

            .isEqualTo(bookExpected);
    }
}
