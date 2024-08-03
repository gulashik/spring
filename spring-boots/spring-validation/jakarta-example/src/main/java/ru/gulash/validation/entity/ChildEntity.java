package ru.gulash.validation.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.gulash.validation.entity.group.DefaultReasonOfValidation;
import ru.gulash.validation.entity.group.ParentReasonOfValidation;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChildEntity {
    // todo проверяем на nullable
    @NotNull(
            message = "My Child Violation message", // todo можем указать своё сообщение об ошибке
            groups = DefaultReasonOfValidation.class // todo маркер группы валидации
    )
    private String name;
}
