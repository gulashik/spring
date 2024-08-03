package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssertEqualsTest {
    @Test
    public void test() {
        // todo assertEquals
        Assertions.assertEquals(/*expected*/ 5, /*actual*/ 5, "optional fail message");
        Assertions.assertNotEquals(/*expected*/ 1, /*actual*/ 5, "optional fail message");

        // todo assertIterableEquals
        Assertions.assertIterableEquals(
            List.of("Apple", "Banana", "Orange"),
            List.of("Apple", "Banana", "Orange"),
            "optional fail message"
        );

        // todo assertArrayEquals можно с учётом допустимого ОТКЛОНЕНИЯ
        int[] expectedArray = {1, 2, 3};
        int[] actualArray = {1, 2, 3};
        Assertions.assertArrayEquals(
            expectedArray,
            actualArray,
            "optional fail message"
        );
        double[] expectedArrayDouble = {1.1, 2.2, 3.4};
        double[] actualArrayDouble = {1.1, 2.2, 3.5};
        Assertions.assertArrayEquals(
            expectedArrayDouble,
            actualArrayDouble,
            0.11, /* todo допустимое ОТКЛОНЕНИЕ*/
            "optional fail message"
        );

    }
}
