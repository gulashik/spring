package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssertTrueFalseTest {
    @Test
    public void test() {
        // todo assertTrue/False
        Assertions.assertTrue(/*condition*/true, "optional fail message");
        Assertions.assertFalse(/*condition*/false, "optional fail message");
    }
}
