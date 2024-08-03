package com.gulash.example.assertjdemo.conditions;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;


public class IsConditionTest {
    @Test
    public void testIsCondition() {
        // todo IS + используется вместе с CONDITION(Condition это Predicate + описание)
        // todo allOf проверяет, что все указанные условия выполняются.
        // todo anyOf проверяет, что хотя бы одно из указанных условий выполняется.
        Condition<String> helloCondition = new Condition<>((String s) -> s.startsWith("Hello"),"some description starts with %s %s", "args", "arg2");
        Condition<String> endsWithExclamationCondition = new Condition<>(s -> s.endsWith("!"), "some description ends with '!' %s %s", "args", "arg2");

        Assertions.assertThat("Hello, AssertJ!")
            .is( // todo внутри Condition
                AssertionsForClassTypes.anyOf(helloCondition, endsWithExclamationCondition)
            )
            .is( // todo внутри Condition
                AssertionsForClassTypes.allOf(helloCondition, endsWithExclamationCondition)
            );

        // todo isNot + используется вместе с CONDITION(Condition это Predicate + описание)
        // todo allOf проверяет, что все указанные условия выполняются.
        // todo anyOf проверяет, что хотя бы одно из указанных условий выполняется.
        Condition<String> notHiCondition = new Condition<>((String s) -> s.startsWith("Hi"),"some description starts with %s %s", "args", "arg2");
        Condition<String> notEndsWithQuestionCondition = new Condition<>(s -> s.endsWith("?"), "some description ends with '?' %s %s", "args", "arg2");
        Assertions.assertThat("Hello, AssertJ!")
            .isNot( // todo внутри Condition
                AssertionsForClassTypes.anyOf(notHiCondition, notEndsWithQuestionCondition)
            )
            .isNot( // todo внутри Condition
                AssertionsForClassTypes.allOf(notHiCondition, notEndsWithQuestionCondition)
            );
    }
}
