package com.gulash.example.assertjdemo.conditions;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;


public class MatchesPredicateTest {
    @Test
    void testMatchesCondition() {
        // todo MATCHES + используется вместе с CONDITION(Condition это Predicate + описание)
        Condition<String> startsWithCondition = new Condition<>(
            s -> s.startsWith("A"),
            "starts with A"
        );

        Condition<String> endsWithCondition = new Condition<>(
            s -> s.endsWith("J"),
            "ends with J"
        );

        // todo matches - удовлетворяет предикатам и Condition-ам
        String actual = "AssertJ";
        Assertions.assertThat(actual).matches(
            (String s) -> s.startsWith("A")/*boolean выражение*/ || endsWithCondition.matches(s)/*Condition + matches*/,
            "starts with A or ends with J" /*описание проверки*/
        );
        // проще
        Assertions.assertThat(actual.startsWith("A") || actual.endsWith("J"))
            .as("starts with A or ends with J") /*описание проверки*/
            .isTrue();
    }
}
