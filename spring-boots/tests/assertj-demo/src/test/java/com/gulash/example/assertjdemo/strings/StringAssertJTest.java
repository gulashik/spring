package com.gulash.example.assertjdemo.strings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StringAssertJTest {
    @Test
    void testStringEquality() {
        String actual = "SpringBoot";
        assertThat(actual)
            // todo Null, Empty
            .isNotNull()
            .isNotEmpty()
            //.isNull()
            //.isNullOrEmpty()
            //.isEmpty()


            // todo Equality
            .isEqualTo("SpringBoot")
            .isEqualToIgnoringCase("springboot")

            .isNotEqualTo("Other")
            .isNotSameAs("Other") // using == comparison

            // todo String fx
            .startsWith("Spring")
            .endsWith("Boot")
            .hasSize(10)

            // todo contains много версий
            .contains("Boot", "SpringBoot")
            .containsIgnoringCase("boot")
            .containsAnyOf("Boot", "SpringBoot")
            .doesNotContain("JUnit")
        ;

        assertThat("user@example.com")
            // todo проверка на совпадение регулярного выражения
            .matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
            .as("Строка должна соответствовать формату email");
    }
}
