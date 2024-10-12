package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertSameTest {
    @Test
    public void testAssertSame() {
        String a = "a";
        String sameA = a;
        String notSameA = new String("a");

        // todo assertSame проверяет, что два объекта — это один и тот же объект (ссылочная проверка)
        Assertions.assertSame(a, sameA, "optional fail message");

        // todo assertNotSame проверяет, что два объекта — это разные объекты (не ссылаются на одну и ту же область памяти)
        Assertions.assertNotSame(a, notSameA, "optional fail message");
    }
}
