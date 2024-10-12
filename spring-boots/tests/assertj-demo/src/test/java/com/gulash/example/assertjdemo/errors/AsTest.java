package com.gulash.example.assertjdemo.errors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AsTest {
    @Disabled
    @DisplayName("Test failed it is OK")
    @Test
    public void testWillFail() {
        // as() is used to describe the test and will be shown before the error message
        int someVar = 22;
        assertThat(someVar)
            .as("при ошибке будет выведено сообщение. можно использовать подстановку %s", someVar)
            .isEqualTo(33);
    }
}
