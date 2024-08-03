package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class AssertLinesMatchTest {
    @Test
    public void assertLinesMatch() {
        // todo assertLinesMatch -  для проверки, что две коллекции строк (или списки строк) содержат ОДИНАКОВЫЕ строки в ОДИНАКОВОМ порядке.
        // шаблон assertLinesMatch(expectedLines, actualLines, "опциональное сообщение об ошибке");

        Assertions.assertLinesMatch(
            List.of("Hello", "World")/*expected*/,
            List.of("Hello", "World")/*actual*/,
            () -> "Опционально Supplier or String failed message"
        );
    }
}
