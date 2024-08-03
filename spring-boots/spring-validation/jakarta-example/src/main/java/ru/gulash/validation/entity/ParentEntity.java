package ru.gulash.validation.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.gulash.validation.entity.group.ParentReasonOfValidation;

@AllArgsConstructor
@Data
public class ParentEntity {
    // todo проверяем на nullable
    @NotNull(
            message = "My Parent Violation message", // todo можем указать своё сообщение об ошибке
            groups = ParentReasonOfValidation.class // todo маркер группы валидации
    )
    private String name;

    private String someAttrWithOutAnnotation;

    @Valid // todo нужна валидация внутри
    @NotNull(
            groups = ParentReasonOfValidation.class
    )
    private ChildEntity child;
}
