package ru.gulash.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.gulash.validation.entity.ChildEntity;
import ru.gulash.validation.entity.ParentEntity;
import ru.gulash.validation.entity.group.DefaultReasonOfValidation;

import java.util.Set;

// todo явное создание
public class ValidatorUnitTest {
    // todo создаём экземпляр явно
    // Kotlin
    // val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    Validator validator  = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void should_fail_when_null() {
        ParentEntity parent = new ParentEntity(/*name*/null, /*name2*/null, /*child*/new ChildEntity());

        // todo использование
        Set<ConstraintViolation<ParentEntity>> actual = validator.validate(parent, DefaultReasonOfValidation.class);

        Assertions.assertThat(actual.size()).isEqualTo(1);
        Assertions.assertThat(actual.stream().toList().get(0).getMessage()).isEqualTo("My Child Violation message");
    }
}
