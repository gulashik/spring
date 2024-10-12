package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssertNullTest {
    @Test
    public void test() {
        // todo NULL check
        Assertions.assertNull( null, "optional fail message");
        Assertions.assertNotNull( "-", "optional fail message");
    }
}
