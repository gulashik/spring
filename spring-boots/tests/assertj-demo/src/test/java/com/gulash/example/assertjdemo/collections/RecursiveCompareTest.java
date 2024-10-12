package com.gulash.example.assertjdemo.collections;

import com.gulash.example.assertjdemo.entity.Book;
import com.gulash.example.assertjdemo.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class RecursiveCompareTest {

    @Test
    void testSaveAndFind() {
        Book bookActual = new Book(1L, "my title", new User(1L,"author1"),"12345", 1, 2);
        Book bookActual1 = new Book(2L, "my title", new User(1L,"author1"),"12345", 1, 2);

        Book bookExpected = new Book(10L, "my TITLE", new User(1L,"author1"),"54321",1, 2);
        Book bookExpected1 = new Book(10L, "my TITLE", new User(1L,"author1"),"54321", 1, 2);

        assertThat(List.of(bookActual,bookActual1))
            // todo Настройки для сравнения=============
            // todo Метод рекурсивно обходит поля объектов и сравнивает их значения.=========
            .usingRecursiveComparison()
            .ignoringFields("id","isbn")
            .withComparatorForType(String.CASE_INSENSITIVE_ORDER, String.class)

            // todo игнорируем порядок элементов в коллекции
            .ignoringCollectionOrder()

            // todo Сравниваем с учётом настроек===========
            .isEqualTo(List.of(bookExpected1,bookExpected));
    }
}
