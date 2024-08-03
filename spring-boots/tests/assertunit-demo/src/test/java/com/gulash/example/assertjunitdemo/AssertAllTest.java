package com.gulash.example.assertjunitdemo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AssertAllTest {
    @Test
    public void test() {
        // todo assertAll
        Assertions.assertAll(
            List.of(1,2,3)
                .stream() /*нужно в stream*/
                .map( (item) -> /*Executable*/() -> Assertions.assertTrue(item > 0) )
        );
        Assertions.assertAll(
            "Опционально описание групппы проверок",
            /*Executable*/
            () -> Assertions.assertEquals(5, 5, "fail message"),
            () -> Assertions.assertEquals(5, 5, "fail message")
        );
    }
}
