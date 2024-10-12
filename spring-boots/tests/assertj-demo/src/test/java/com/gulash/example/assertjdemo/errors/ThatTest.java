package com.gulash.example.assertjdemo.errors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThatTest {
    @Test
    void testThat() {
        // todo Message
        Assertions.assertThat(new IllegalArgumentException("wrong amount 123"))
            .hasMessage("wrong amount 123")
            .hasMessage("%s amount %d", "wrong", 123)
            // check start
            .hasMessageStartingWith("wrong")
            .hasMessageStartingWith("%s a", "wrong")
            // check content
            .hasMessageContaining("wrong amount")
            .hasMessageContaining("wrong %s", "amount")
            .hasMessageContainingAll("wrong", "amount")
            // check end
            .hasMessageEndingWith("123")
            .hasMessageEndingWith("amount %s", "123")
            // check with regex
            .hasMessageMatching("wrong amount .*")
            // check does not contain
            .hasMessageNotContaining("right")
            .hasMessageNotContainingAny("right", "price");

        // todo Cause
        NullPointerException cause = new NullPointerException("boom!");
        Throwable throwable = new Throwable(cause);

        Assertions.assertThat(throwable).hasCause(cause)
            // hasCauseInstanceOf will match inheritance.
            .hasCauseInstanceOf(NullPointerException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            // hasCauseExactlyInstanceOf will match only exact same type
            .hasCauseExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThat(throwable).cause()
            // isInstanceOf will match inheritance.
            .isInstanceOf(NullPointerException.class)
            .isInstanceOf(RuntimeException.class)
            // isExactlyInstanceOf will match only exact same type
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
